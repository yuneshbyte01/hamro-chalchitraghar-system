package com.hamrochalchitraghar.system.repository;

import com.hamrochalchitraghar.system.model.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {}
