import { ref } from 'vue'
export default function () {
  let num = ref(1)
  let fav = ref(false)
  let favBtn = () => {
    num.value++
    fav.value = true
    setTimeout(() => {
      fav.value = false
    }, 2000)
  }
  return {
    num,
    fav,
    favBtn
  }
}
