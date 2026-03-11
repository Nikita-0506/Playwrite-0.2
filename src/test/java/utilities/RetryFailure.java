package utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

//@Author: neha.verma@inadev.com
//@Date: 12 July 2025
//@Desc: This class holds functionality to attempt retry on failures


public class RetryFailure implements IRetryAnalyzer {
    private int retryCount = 0;
    private static final int maxRetry = 0;
    private static final Logger log = LogManager.getLogger(RetryFailure.class);

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < maxRetry) {
            retryCount++;
            log.warn("Retrying test '{}' (retry {}/{})", result.getName(), retryCount, maxRetry);

            return true;
        }
        return false;
    }
}
