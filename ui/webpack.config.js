const path = require('path')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const merge = require('webpack-merge')
const webpack = require('webpack')

let config = {
  entry: ['./src/main/webapp/index.js'],
  output: {
    filename: 'replication.bundle.js',
    path: path.resolve(__dirname, 'target', 'webapp'),
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: ['babel-loader'],
      },
      {
        test: /\.html$/,
        use: [
          {
            loader: 'html-loader',
            options: { minimize: true },
          },
        ],
      },
      {
        test: /\.css$/,
        use: [
          'style-loader',
          {
            loader: 'css-loader',
            options: {
              sourceMap: true,
              modules: true,
              importLoaders: 1,
              localIdentName: '[name]__[local]___[hash:base64:5]',
            },
          },
        ],
      },
    ],
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: './src/main/webapp/index.html',
      filename: 'index.html',
    }),
  ],
}

module.exports = (env, argv) => {
  if (argv.mode === 'production') {
    // Production specific configuration
  }

  if (argv.mode === 'development') {
    config = merge.smart(config, {
      devtool: 'source-map',
      mode: 'development',
      plugins: [new webpack.HotModuleReplacementPlugin()],
      devServer: {
        publicPath: '/admin/replication/',
        openPage: 'admin/replication/',
        hotOnly: true,
        inline: true,
        disableHostCheck: true,
        proxy: {
          '/admin/hub': {
            target: 'https://localhost:8993',
            secure: false,
            auth: 'admin:admin',
            headers: {
              Origin: 'https://localhost:8993',
            },
          },
        },
        watchOptions: {
          ignored: '/node_modules/',
        },
      },
    })
  }

  return config
}
