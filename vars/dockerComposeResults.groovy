def call(
        String service,
        String testReportsDir = null,
        boolean allowEmptyResults = false,
        double healthScaleFactor = 1.0,
        def perfReportConstraints = null
) {
    docker.withTool('docker') {
        def CONTAINER = sh(returnStdout: true, script: "docker-compose ps -q ${service}").trim()
        def exit_code = sh(returnStdout: true, script: "docker wait ${CONTAINER}").trim()
        if (testReportsDir != null) {
            def copyExitCode = sh(returnStatus: true, script: $/
docker cp ${CONTAINER}:${testReportsDir}/ ./results
SED_COMMAND="s:$(echo -n "[[ATTACHMENT|${testReportsDir}" | sed 's/[\[\:&.*]/\\&/g'):$(echo -n "[[ATTACHMENT|$(pwd)/results" | sed 's/[:&]/\\&/g'):g"
find ./results -name '*.xml' -exec sed -i "$${SED_COMMAND}" {} \;
/$)
            if (copyExitCode != 0) {
                echo("WARNING: Fetching test results from container failed with ${copyExitCode}")
            }
            Map<String, Integer> testCount = recordJunit(testResults: 'results/**/*.xml', allowEmptyResults: allowEmptyResults, healthScaleFactor: healthScaleFactor, testDataPublishers: [[$class: 'AttachmentPublisher']])
            List kv = testCount.collect { String name, Integer count ->
                return "${count} ${name}"
            }
            echo "Recorded tests: ${kv.join(', ')}"
        }
        if (exit_code != "0") {
            error("Service ${service} exited with code ${exit_code}")
        }
    }
    if (perfReportConstraints != null) {
        echo "perfReportConstraints parameter is deprecated, use performanceReport(perfConstraints) instead"
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
}

def call(args) {
        return call(args.service, args.testReportsDir, args.allowEmptyResults || false, (args.healthScaleFactor ?: 1.0d), args.perfReportConstraints ?: null)
}

