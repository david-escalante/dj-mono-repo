import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    host: "127.0.0.1",
    port: 3847,
    proxy: {
      "/api": "http://127.0.0.1:8091",
      "/actuator": "http://127.0.0.1:8091",
    },
  },
  preview: {
    host: "127.0.0.1",
    port: 3847,
    proxy: {
      "/api": "http://127.0.0.1:8091",
      "/actuator": "http://127.0.0.1:8091",
    },
  },
});
