// Custom Karma configuration for running in containerized environments
config.set({
    browsers: ['ChromeHeadlessNoSandbox'],
    customLaunchers: {
        ChromeHeadlessNoSandbox: {
            base: 'ChromeHeadless',
            flags: ['--no-sandbox', '--disable-gpu', '--disable-dev-shm-usage']
        }
    }
});
