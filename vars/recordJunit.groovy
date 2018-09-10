import hudson.tasks.test.AbstractTestResultAction

Map<String,Integer> call(Map props) {
    int total = 0
    int failed = 0
    int skipped = 0
    try {
        AbstractTestResultAction testResultAction = currentBuild.rawBuild.getAction(AbstractTestResultAction.class)
        if (testResultAction != null) {
            total = testResultAction.getTotalCount()
            failed = testResultAction.getFailCount()
            skipped = testResultAction.getSkipCount()
        }
    } catch (ignored) {
    }

    junit(props)

    try {
        AbstractTestResultAction testResultAction = currentBuild.rawBuild.getAction(AbstractTestResultAction.class)
        if (testResultAction != null) {
            total = testResultAction.getTotalCount() - total
            failed = testResultAction.getFailCount() - failed
            skipped = testResultAction.getSkipCount() - skipped
        }
    } catch (ignored) {
    }
    return [
        total: total,
        failed: failed,
        skipped: skipped,
    ]
}
