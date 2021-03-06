package com.ociweb.gl.test;

import com.ociweb.pronghorn.stage.scheduling.ElapsedTimeRecorder;
import com.ociweb.pronghorn.util.Appendables;

public class DefaultParallelClientLoadTesterOutput implements ParallelClientLoadTesterOutput {

	StringBuilder progress = new StringBuilder();
	
    @Override
    public void progress(int percentDone, long sumTimeouts, long sumInvalid) {
    	
    	progress.setLength(0);
    	
        Appendables.appendValue(progress, percentDone);
        progress.append("% complete  ");
        Appendables.appendValue(progress, sumTimeouts);
        progress.append(" failed  ");
        Appendables.appendValue(progress, sumInvalid);
        progress.append(" invalid ");
        Appendables.appendEpochTime(progress, System.currentTimeMillis());
        progress.append("\n");
        System.out.append(progress);
    }

    @Override
    public void end(
            ElapsedTimeRecorder etr, long testDuration, long totalMessages, long totalTimeSumNS, long serverCallsPerSecond,
            long sendAttempts, long sendFailures, long timeouts, long responsesReceived, long invalidResponses) {
        try {
            Thread.sleep(100); //fixing system out IS broken problem.
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Appendables.appendNearestTimeUnit(System.out, testDuration).append(" test duration\n");
        Appendables.appendValue(System.out, serverCallsPerSecond).append(" total calls per second against server\n");

        System.out.println();
        etr.report(System.out).append("\n");
             
        if (totalMessages>0) {
        	long avgLatencyNS = totalTimeSumNS/(long)totalMessages;
        	Appendables.appendNearestTimeUnit(System.out, avgLatencyNS).append(" average\n");
        } else {
        	System.out.println("warning: zero messages tested");
        }

        System.out.println("Total messages: " + totalMessages);
        System.out.println("Send failures: " + sendFailures + " out of " + sendAttempts);
        System.out.println("Timeouts: " + timeouts);
        System.out.println("Responses invalid: " + invalidResponses + " out of " + responsesReceived);
        System.out.println();
    }

    @Override
    public void connectionClosed(int track) {
        System.out.println("Connection Closed: " + track);
    }

    @Override
    public void failedToStart(int maxInFlight) {
        System.err.println("Unable to send "+maxInFlight+" messages to start up.");
    }
}
