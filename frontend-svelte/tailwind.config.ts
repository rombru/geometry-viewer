import type {Config} from 'tailwindcss';

export default {
  content: ['./src/**/*.{html,js,svelte,ts}'],

  theme: {
    extend: {}
  },

  // @ts-ignore
  plugins: [require('daisyui')]
} satisfies Config;
