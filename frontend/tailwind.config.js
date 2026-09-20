/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        zinc: {
          950: '#09090b',
          900: '#121216',
          850: '#18181c',
          800: '#222228',
          700: '#2e2e38',
        },
      },
      boxShadow: {
        'inner-glow': 'inset_0_1px_0_0_rgba(255,255,255,0.06)',
        'card-hover': '0 12px 40px -10px rgba(0,0,0,0.5)',
      },
    },
  },
  plugins: [],
}
