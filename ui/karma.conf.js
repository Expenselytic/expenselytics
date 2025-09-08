module.exports = function (config) {
  const isMaven = process.env.MAVEN_BUILD === 'true';

  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-json-reporter'),
      require('@angular-devkit/build-angular/plugins/karma')
    ],
    client: { clearContext: false },
    reporters: isMaven ? ['progress', 'json'] : ['progress'],
    jsonReporter: {
      stdout: false,
      outputFile: 'karma-results.json',
      separator: ','
    },
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: !isMaven,
    singleRun: isMaven,
    restartOnFileChange: !isMaven,
    browsers: isMaven ? ['ChromeHeadless'] : ['Chrome'],
    customLaunchers: {
      ChromeHeadlessCustom: {
        base: 'ChromeHeadless',
        flags: [
          '--no-sandbox',
          '--disable-gpu',
          '--disable-extensions',
          '--remote-debugging-port=9222'
        ]
      }
    }
  });
};
