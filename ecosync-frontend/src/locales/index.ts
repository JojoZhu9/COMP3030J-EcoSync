import { createI18n } from 'vue-i18n'
import en from './en.json'
import zh from './zh.json'

const savedLocale = localStorage.getItem('locale') || 'en'

export const i18n = createI18n({
  legacy: false,
  locale: savedLocale,
  fallbackLocale: 'en',
  messages: { en, zh }
})

export type Locale = 'en' | 'zh'

export function setLocale(locale: Locale) {
  localStorage.setItem('locale', locale)
  i18n.global.locale.value = locale
}

export function getLocale(): Locale {
  return (localStorage.getItem('locale') as Locale) || 'en'
}
