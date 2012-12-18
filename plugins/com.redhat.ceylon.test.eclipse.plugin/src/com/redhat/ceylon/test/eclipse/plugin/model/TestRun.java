package com.redhat.ceylon.test.eclipse.plugin.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.ILaunch;

import com.redhat.ceylon.test.eclipse.plugin.model.TestElement.State;
import com.redhat.ceylon.test.eclipse.plugin.runner.RemoteTestEvent;

public class TestRun {

    private final ILaunch launch;
    private final List<TestElement> testElements = new ArrayList<TestElement>();
    private boolean isRunning;
    private boolean isFinished;
    private boolean isInterrupted;
    private int startedCount = 0;
    private int successCount = 0;
    private int failureCount = 0;
    private int errorCount = 0;

    public TestRun(ILaunch launch) {
        this.launch = launch;
    }

    public ILaunch getLaunch() {
        return launch;
    }
    
    public List<TestElement> getTestElements() {
        return testElements;
    }
    
    public boolean isRunning() {
        return isRunning;
    }

    public boolean isFinished() {
        return isFinished;
    }
    
    public boolean isInterrupted() {
        return isInterrupted;
    }

    public boolean isSuccess() {
        return failureCount == 0 && errorCount == 0;
    }

    public int getTotalCount() {
        return testElements.size();
    }

    public int getStartedCount() {
        return startedCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getFinishedCount() {
        return successCount + failureCount + errorCount;
    }

    public void processRemoteTestEvent(RemoteTestEvent event) {
        switch (event.getType()) {
        case TEST_RUN_STARTED:
            testElements.addAll(event.getTestElements());
            isRunning = true;
            isFinished = false;
            isInterrupted = false;
            break;
        case TEST_RUN_FINISHED:
            isRunning = false;
            isFinished = true;
            isInterrupted = false;
            break;
        case TEST_STARTED:
            updateTestElement(event.getTestElement());
            startedCount++;
            break;
        case TEST_FINISHED:
            updateTestElement(event.getTestElement());
            updateCounters(event);
            break;
        }
    }

    public void processLaunchTerminatedEvent() {
        if( isRunning ) {
            for (TestElement testElement : testElements) {
                if (testElement.getState() == State.RUNNING) {
                    testElement.setState(State.UNDEFINED);
                }
            }
            isRunning = false;
            isFinished = false;
            isInterrupted = true;
        }
    }

    private void updateTestElement(TestElement testElement) {
        int index = testElements.indexOf(testElement);
        testElements.set(index, testElement);
    }

    private void updateCounters(RemoteTestEvent event) {
        State state = event.getTestElement().getState();
        switch (state) {
        case SUCCESS:
            successCount++;
            break;
        case FAILURE:
            failureCount++;
            break;
        case ERROR:
            errorCount++;
            break;
        default:
            throw new IllegalStateException(event.toString());
        }
    }

}