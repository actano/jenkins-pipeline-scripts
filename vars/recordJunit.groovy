Map<String,Integer> call(Map props) {
    AbstractTestResultAction testResultAction = currentBuild.rawBuild.getAction(AbstractTestResultAction.class)
    int total = testResultAction == null ? 0 : testResultAction.getTotalCount()
    int failed = testResultAction == null ? 0 : testResultAction.getFailCount()
    int skipped = testResultAction == null ? 0 : testResultAction.getSkipCount()

    junit(props)

    testResultAction = currentBuild.rawBuild.getAction(AbstractTestResultAction.class)
    total = (testResultAction == null ? 0 : testResultAction.getTotalCount()) - total
    failed = (testResultAction == null ? 0 : testResultAction.getFailCount()) - failed
    skipped = (testResultAction == null ? 0 : testResultAction.getSkipCount()) - skipped
    return [
        total: total,
        failed: failed,
        skipped: skipped,
    ]
}
