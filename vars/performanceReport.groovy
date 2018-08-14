def call(
        def perfReportConstraints = null
) {
    def buildResultBeforePerfReport = currentBuild.currentResult
    perfReport(
            sourceDataFiles: 'results/**/*.xml',
            constraints: perfReportConstraints,
            modeEvaluation: true,
    )
    def buildResultAfterPerfReport = currentBuild.currentResult
    if ((buildResultAfterPerfReport == 'FAILURE' || buildResultAfterPerfReport == 'UNSTABLE') && (buildResultBeforePerfReport != buildResultAfterPerfReport)) {
        // we can not be 100 percent sure if this step has failed the build but it is our best guess
        // because steps can be executed in parallel
        error("Performance Report failed")
    }
}
