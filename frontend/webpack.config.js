const webpack = require("webpack");
const CopyPlugin = require("copy-webpack-plugin");
const path = require("path");
const targetDir = path.join(__dirname, "../backend/src/main/resources/static/");

module.exports = {
  mode: process.env.NODE_ENV === "development" ? "development" : "production",
  entry: "./main.js",
  output: {
    path: targetDir,
    filename: "bundle.js",
  },
  plugins: [
    new CopyPlugin({
      patterns: [
        { from: "./index.html", to: targetDir },
      ],
    }),
  ],
};
