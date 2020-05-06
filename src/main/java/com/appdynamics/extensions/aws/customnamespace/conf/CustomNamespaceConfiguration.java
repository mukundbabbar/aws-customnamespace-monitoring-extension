/*
 * Copyright 2020. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.aws.customnamespace.conf;

import com.appdynamics.extensions.aws.config.Configuration;

import java.util.List;

/**
 * @author Prashant Mehta
 *
 */
public class CustomNamespaceConfiguration extends Configuration {
	private List<AWSAccount> awsAccounts;

	private String encryptionKey;

	public List<AWSAccount> getAwsAccounts() {
		return awsAccounts;
	}

	public void setAwsAccounts(List<AWSAccount> awsAccounts) {
		this.awsAccounts = awsAccounts;
	}

	public String getEncryptionKey() {
		return encryptionKey;
	}

	public void setEncryptionKey(String encryptionKey) {
		this.encryptionKey = encryptionKey;
	}
}
