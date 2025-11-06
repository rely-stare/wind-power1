import { fileURLToPath, URL } from 'node:url'
import path from 'node:path'
import Components from 'unplugin-vue-components/vite'
import { AntDesignVueResolver } from 'unplugin-vue-components/resolvers'
import AutoImport from 'unplugin-auto-import/vite'
import VueSetupExtend from 'vite-plugin-vue-setup-extend'
import copy from 'rollup-plugin-copy'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'

// https://vitejs.dev/config/
export default (mode) => {
  const env = loadEnv(mode, process.cwd())
  return defineConfig({
    plugins: [
      vue(),
      VueSetupExtend(),
      vueJsx(),
      AutoImport({
        imports: ['vue', 'vue-router']
      }),
      copy({
        targets: [
          {
            src: 'node_modules/@liveqing/liveplayer-v3/dist/component/liveplayer-lib.min.js',
            dest: 'public/js'
          }
        ]
      }),
      Components({
        resolvers: [
          AntDesignVueResolver({
            importStyle: false // css in js
          })
        ], // antd按需引入
        dirs: ['src/components'] // 自定义全局组件按需引入
      })
      // 压缩配置
      // viteCompression({
      //   verbose: true, // 默认即可
      //   disable: false, // 开启压缩(不禁用)，默认即可
      //   deleteOriginFile: false, // 删除源文件
      //   threshold: 10240, // 压缩前最小文件大小
      //   algorithm: 'gzip', // 压缩算法
      //   ext: '.gz' // 文件类型
      // })
    ],
    // envDir: path.resolve(__dirname, './env'),
    build: {
      outDir: 'dist',
      minify: 'terser',
      terserOptions: {
        compress: {
          keep_infinity: true, // 防止 Infinity 被压缩成 1/0，这可能会导致 Chrome 上的性能问题
          drop_console: true, // 生产环境去除 console
          drop_debugger: true // 生产环境去除 debugger
        }
      },
      rollupOptions: {
        //打包文件目录整理

        output: {
          chunkFileNames: 'static/js/[name]-[hash].js',
          entryFileNames: 'static/js/[name]-[hash].js',
          // assetFileNames: 'static/[ext]/[name]-[hash].[ext]' 开启会出现异常bug
          manualChunks(id) {
            if (id.includes('node_modules')) {
              // 超大静态资源拆分
              return id.toString().split('node_modules/')[1].split('/')[0].toString()
            }
          }
        }
      },
      chunkSizeWarningLimit: 1500 // chunk 大小警告的限制（以 kbs 为单位）
    },
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    base: '/',
    css: {
      preprocessorOptions: {
        scss: { additionalData: '@import "@/assets/style/handle.scss";' }
      }
    },
    server: {
      host: '0.0.0.0',
      proxy: {
        '/auth/util': {
          target: env.VITE_BASE_API,
          // target: 'http://172.17.1.67:20021/',
          rewrite: (path) => path.replace(/^\/auth\/util/, '/util')
        },
        '/auth/system': {
          target: env.VITE_BASE_API,
          // target: 'http://172.17.1.67:20021/',uiu
          rewrite: (path) => path.replace(/^\/auth\/system/, '/system')
        },
        '/auth/camera': {
          target: env.VITE_BASE_API,
          // target: 'http://172.17.1.67:20021/',uiu
          rewrite: (path) => path.replace(/^\/auth\/camera/, '/camera')
        },
        '/auth': {
          target: env.VITE_BASE_API,
          changeOrigin:true,
          rewrite: (path) => path.replace(/^\/auth/, '')
        },
    },
  }
  })
}
