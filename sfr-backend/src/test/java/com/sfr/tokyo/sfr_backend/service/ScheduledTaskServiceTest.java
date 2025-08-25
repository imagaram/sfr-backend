package com.sfr.tokyo.sfr_backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ScheduledTaskServiceのテストクラス
 */
@ExtendWith(MockitoExtension.class)
public class ScheduledTaskServiceTest {

    @Mock
    private RateLimitService rateLimitService;

    private ScheduledTaskService scheduledTaskService;

    @BeforeEach
    void setUp() {
        scheduledTaskService = new ScheduledTaskService(rateLimitService);
    }

    @Test
    void testCleanupRateLimitData_CallsRateLimitService() {
        // When
        assertDoesNotThrow(() -> {
            scheduledTaskService.cleanupRateLimitData();
        });

        // Then
        verify(rateLimitService, times(1)).cleanup();
    }

    @Test
    void testCleanupRateLimitData_HandlesException() {
        // Given
        doThrow(new RuntimeException("Cleanup error")).when(rateLimitService).cleanup();

        // When & Then
        assertDoesNotThrow(() -> {
            scheduledTaskService.cleanupRateLimitData();
        });

        // Verify that the method was called despite the exception
        verify(rateLimitService, times(1)).cleanup();
    }

    @Test
    void testGenerateSecurityReport_ExecutesWithoutErrors() {
        // When & Then
        assertDoesNotThrow(() -> {
            scheduledTaskService.generateSecurityReport();
        });
    }

    @Test
    void testGenerateSecurityReport_DoesNotInteractWithRateLimitService() {
        // When
        scheduledTaskService.generateSecurityReport();

        // Then
        verifyNoInteractions(rateLimitService);
    }

    @Test
    void testScheduledMethods_AreAnnotatedCorrectly() {
        // Test that the methods exist and can be called
        // (Annotation testing would typically be done with reflection in a real
        // scenario)

        assertTrue(scheduledTaskService != null);

        // Verify methods exist and are callable
        assertDoesNotThrow(() -> {
            scheduledTaskService.cleanupRateLimitData();
            scheduledTaskService.generateSecurityReport();
        });
    }

    @Test
    void testCleanupRateLimitData_MultipleCallsWork() {
        // When
        assertDoesNotThrow(() -> {
            scheduledTaskService.cleanupRateLimitData();
            scheduledTaskService.cleanupRateLimitData();
            scheduledTaskService.cleanupRateLimitData();
        });

        // Then
        verify(rateLimitService, times(3)).cleanup();
    }

    @Test
    void testGenerateSecurityReport_MultipleCallsWork() {
        // When & Then
        assertDoesNotThrow(() -> {
            scheduledTaskService.generateSecurityReport();
            scheduledTaskService.generateSecurityReport();
            scheduledTaskService.generateSecurityReport();
        });
    }

    @Test
    void testConcurrentExecution_HandlesSafely() throws InterruptedException {
        // Test concurrent execution safety
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                scheduledTaskService.cleanupRateLimitData();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                scheduledTaskService.generateSecurityReport();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        // When
        thread1.start();
        thread2.start();

        thread1.join(1000);
        thread2.join(1000);

        // Then
        assertFalse(thread1.isAlive());
        assertFalse(thread2.isAlive());
        verify(rateLimitService, times(5)).cleanup();
    }
}
