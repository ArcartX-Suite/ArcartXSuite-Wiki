import { createPreset } from 'fumadocs-ui/tailwind-plugin';

const suitePalette = {
  background: '260 10% 11%',
  foreground: '265 12% 87%',
  muted: '260 10% 15%',
  'muted-foreground': '265 9% 67%',
  popover: '260 12% 14%',
  'popover-foreground': '265 12% 87%',
  card: '260 11% 13%',
  'card-foreground': '265 12% 87%',
  border: '260 10% 23%',
  primary: '39 56% 72%',
  'primary-foreground': '270 15% 11%',
  secondary: '260 12% 17%',
  'secondary-foreground': '265 12% 87%',
  accent: '267 22% 20%',
  'accent-foreground': '276 40% 89%',
  ring: '263 36% 73%',
};

/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    './components/**/*.{ts,tsx}',
    './app/**/*.{ts,tsx}',
    './content/**/*.{md,mdx}',
    './mdx-components.{ts,tsx}',
    './node_modules/fumadocs-ui/dist/**/*.js',
  ],
  presets: [createPreset({ preset: { light: suitePalette, dark: suitePalette } })],
  theme: {
    extend: {
      fontFamily: {
        sans: ['-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Microsoft YaHei UI', 'Microsoft YaHei', 'sans-serif'],
        mono: ['Cascadia Code', 'Consolas', 'SFMono-Regular', 'ui-monospace', 'monospace'],
      },
    },
  },
};
