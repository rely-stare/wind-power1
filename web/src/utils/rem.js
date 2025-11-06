const autoFont = () => {
  const fontBase = 192
  const designWidth = 1080
  const html = document.querySelector('html')
  let currentWidth = 0
  //判断是pc端，就设置固定宽度为750，可自行调整
  currentWidth = 1920

  const currentHeight = html.clientHeight
  if (currentWidth > currentHeight) {
    currentWidth = currentHeight
  }
  const currentFontWidth = (fontBase * currentWidth) / designWidth
  html.style.fontSize = currentFontWidth + 'px'
}
export default autoFont
