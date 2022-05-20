/*
* Copyright (c) 2NO_ERROR21 PSPACE, inc. KSAN Development Team ksan@pspace.co.kr
* ifsmover is a suite of free software: you can redistribute it and/or modify it under the terms of
* the GNU General Public License as published by the Free Software Foundation, either version 
* 3 of the License.  See LICENSE for details
*
* 본 프로그램 및 관련 소스코드, 문서 등 모든 자료는 있는 그대로 제공이 됩니다.
* KSAN 프로젝트의 개발자 및 개발사는 이 프로그램을 사용한 결과에 따른 어떠한 책임도 지지 않습니다.
* KSAN 개발팀은 사전 공지, 허락, 동의 없이 KSAN 개발에 관련된 모든 결과물에 대한 LICENSE 방식을 변경 할 권리가 있습니다.
*/
package com.pspace.ifsmover.rest.repository;

import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.pspace.ifsmover.rest.S3Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IfsS3 implements Repository {
    private static final Logger logger = LoggerFactory.getLogger(IfsS3.class);
	private boolean isSource;
    private S3Config config;
    private boolean isAWS;
    private boolean isSecure;
    private AmazonS3 client;
	private String errCode;
	private String errMessage;

	private final String HTTPS = "https";
	private final String AWS_S3_V4_SIGNER_TYPE = "AWSS3V4SignerType";
	private final String BUCKET_ALREADY_OWNED_BY_YOU = "BucketAlreadyOwnedByYou";
	private final String BUCKET_ALREADY_EXISTS = "BucketAlreadyExists";
	private final String INVALID_ACCESS_KEY_ID = "InvalidAccessKeyId";
	private final String SIGNATURE_DOES_NOT_MATCH = "SignatureDoesNotMatch";

	private final int MILLISECONDS = 1000;
	private final int TIMEOUT = 300;
	private final int RETRY_COUNT = 2;

	private final String LOG_SOURCE_INVALID_ACCESS = "source - The access key is invalid.";
	private final String LOG_SOURCE_INVALID_SECRET = "source - The secret key is invalid.";
	private final String LOG_TARGET_INVALID_ACCESS = "target - The access key is invalid.";
	private final String LOG_TARGET_INVALID_SECRET = "target - The secret key is invalid.";
	private final String LOG_SOURCE_ENDPOINT_NULL = "source - endpoint is null";
	private final String LOG_TARGET_ENDPOINT_NULL = "target - endpoint is null";
	private final String LOG_SOURCE_BUCKET_NULL = "source - bucket is null";
	private final String LOG_TARGET_BUCKET_NULL = "target - bucket is null";
	private final String LOG_SOURCE_BUCKET_NOT_EXIST = "source - bucket is not exist";
	private final String LOG_SOURCE_NOT_REGION = "source - unable to find region.";
	private final String LOG_TARGET_NOT_REGION = "target - unable to find region.";
	private final String LOG_SOURCE_INVALID_ENDPOINT = "source - endpoint is invalid.";
	private final String LOG_TARGET_INVALID_ENDPOINT = "target - endpoint is invalid.";

	public IfsS3() {}

    public void setConfig(S3Config config, boolean isSource) {
        this.config = config;
        this.isSource = isSource;
        isAWS = config.isAWS();
        isSecure = isAWS;
    }

	@Override
    public int check() {
		if (config.getEndPoint() == null || config.getEndPoint().isEmpty()) {
			if (isSource) {
				logger.error(LOG_SOURCE_ENDPOINT_NULL);
				errMessage = LOG_SOURCE_ENDPOINT_NULL;
			} else {
				logger.error(LOG_TARGET_ENDPOINT_NULL);
				errMessage = LOG_TARGET_ENDPOINT_NULL;
			}
			return ENDPOINT_IS_NULL;
		}

		if (config.getBucket() == null || config.getBucket().isEmpty()) {
			if (isSource) {
				logger.error(LOG_SOURCE_BUCKET_NULL);
				errMessage = LOG_SOURCE_BUCKET_NULL;
			} else {
				logger.error(LOG_TARGET_BUCKET_NULL);
				errMessage = LOG_TARGET_BUCKET_NULL;
			}
			return BUCKET_IS_NULL;
		}

		if (isAWS) {
			isSecure = true;
		} else {
			if (config.getEndPointProtocol().compareToIgnoreCase(HTTPS) == 0) {
				isSecure = true;
			} else {
				isSecure = false;
			}
		}

        int result = getClient();
		if (result != NO_ERROR) {
			return result;
		}

		if(!isValidBucketName(config.getBucket())) {
			if (isSource) {
				errMessage = "source - bucket name is invalid : " + config.getBucket();
			} else {
				errMessage = "target - bucket name is invalid : " + config.getBucket();
			}
			return 
			INVALID_BUCKET;
		}
		result = existBucket(true, config.getBucket());
		if (result == BUCKET_NO_EXIST) {
			if (isSource) {
				logger.error(LOG_SOURCE_BUCKET_NOT_EXIST);
				errMessage = LOG_SOURCE_BUCKET_NOT_EXIST;
				return BUCKET_NO_EXIST;
			} else {
				result = createBucket(true);
				if (result != NO_ERROR) {
					return result;
				}
			}
		} else if (result != NO_ERROR) {
			return result;
		}

        return NO_ERROR;
    }

    public int getClient() {
        try {
			client = createClient(isAWS, isSecure, config.getEndPoint(), config.getAccessKey(), config.getSecretKey());
		} catch (SdkClientException e) {
			if (isSource) {
				logger.error(LOG_SOURCE_NOT_REGION);
				errMessage = LOG_SOURCE_NOT_REGION;
			} else {
				logger.error(LOG_TARGET_NOT_REGION);
				errMessage = LOG_TARGET_NOT_REGION;
			}
			
            return UNABLE_FIND_REGION;
		} catch (IllegalArgumentException e) {
			if (isSource) {
				logger.error(LOG_SOURCE_INVALID_ENDPOINT);
				errMessage = LOG_SOURCE_INVALID_ENDPOINT;
			} else {
				logger.error(LOG_TARGET_INVALID_ENDPOINT);
				errMessage = LOG_TARGET_INVALID_ENDPOINT;
			}
            return INVALID_ENDPOINT;
		}

        return NO_ERROR;
    }

    private AmazonS3 createClient(boolean isAWS, boolean isSecure, String URL, String AccessKey, String SecretKey) throws SdkClientException, IllegalArgumentException{
		ClientConfiguration config;

		if (isSecure) {
			config = new ClientConfiguration().withProtocol(Protocol.HTTPS);
		} else {
			config = new ClientConfiguration().withProtocol(Protocol.HTTP);
		}

		config.setSignerOverride(AWS_S3_V4_SIGNER_TYPE);
		config.setMaxErrorRetry(RETRY_COUNT);
		config.setConnectionTimeout(TIMEOUT * MILLISECONDS);
		config.setSocketTimeout(TIMEOUT * MILLISECONDS);
		AmazonS3ClientBuilder clientBuilder = AmazonS3ClientBuilder.standard();

		clientBuilder.setCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(AccessKey, SecretKey)));
		
		if (isAWS) {
			clientBuilder.setRegion(URL);
		} else {
			clientBuilder.setEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(URL, ""));
		}
		
		clientBuilder.setClientConfiguration(config);
		clientBuilder.setPathStyleAccessEnabled(true);
		
		return clientBuilder.build();
	}

	private static final CharMatcher VALID_BUCKET_CHAR =
		CharMatcher.inRange('a', 'z')
		.or(CharMatcher.inRange('0', '9'))
		.or(CharMatcher.is('-'))
		.or(CharMatcher.is('.'));

	private boolean validateIpAddress(String string) {
		List<String> parts = Splitter.on('.').splitToList(string);
		if (parts.size() != 4) {
			return false;
		}
		for (String part : parts) {
			try {
				int num = Integer.parseInt(part);
				if (num < 0 || num > 255) {
					return false;
				}
			} catch (NumberFormatException nfe) {
				return false;
			}
		}
		return true;
	}
			
	private boolean isValidBucketName(String bucketName) {
		if (bucketName == null ||
				bucketName.length() < 3 || bucketName.length() > 255 ||
				bucketName.startsWith(".") || bucketName.startsWith("-") ||
				bucketName.endsWith(".") || bucketName.endsWith("-") || validateIpAddress(bucketName) ||
				!VALID_BUCKET_CHAR.matchesAllOf(bucketName) ||
				bucketName.startsWith("xn--") || bucketName.contains("..") ||
				bucketName.contains(".-") || bucketName.contains("-.")) {

			return false;
		}

		return true;
	}

    private int existBucket(boolean isCheck, String bucket) {
		int result = NO_ERROR;
		logger.info("check exists bucket : {}", bucket);
		try {
			if (client.doesBucketExistV2(bucket)) {
				result = NO_ERROR;
			} else {
				result = BUCKET_NO_EXIST;
			}
		} catch (AmazonServiceException ase) {
			errCode = ase.getErrorCode();
			switch (errCode) {
			case INVALID_ACCESS_KEY_ID:
				if (isCheck) {
					if (isSource) {
						logger.error(LOG_SOURCE_INVALID_ACCESS);
						errMessage = LOG_SOURCE_INVALID_ACCESS;
					} else {
						logger.error(LOG_TARGET_INVALID_ACCESS);
						errMessage = LOG_TARGET_INVALID_ACCESS;
					}
				} else {
					if (isSource) {
						logger.error(LOG_SOURCE_INVALID_ACCESS);
						errMessage = LOG_SOURCE_INVALID_ACCESS;
					} else {
						logger.error(LOG_TARGET_INVALID_ACCESS);
						errMessage = LOG_TARGET_INVALID_ACCESS;
					}
				}
				result = INVALID_ACCESS_KEY;
				break;
				
			case SIGNATURE_DOES_NOT_MATCH:
				if (isCheck) {
					if (isSource) {
						logger.error(LOG_SOURCE_INVALID_SECRET);
						errMessage = LOG_SOURCE_INVALID_SECRET;
					} else {
						logger.error(LOG_TARGET_INVALID_SECRET);
						errMessage = LOG_TARGET_INVALID_SECRET;
					}
				} else {
					if (isSource) {
						logger.error(LOG_SOURCE_INVALID_SECRET);
						errMessage = LOG_SOURCE_INVALID_SECRET;
					} else {
						logger.error(LOG_TARGET_INVALID_SECRET);
						errMessage = LOG_TARGET_INVALID_SECRET;
					}
				}
				result = INVALID_SECRET_KEY;
				break;
			}
        } catch (AmazonClientException ace) {
        	if (isCheck) {
        		if (isSource) {
					logger.error("source - {}", ace.getMessage());
					errMessage = "source - " + ace.getMessage();
        		} else {
					logger.error("target - {}", ace.getMessage());
					errMessage = "target - " + ace.getMessage();
        		}
			}
			result = AMAZON_CLIENT_EXCEPTION;
        }
		
		return result;
	}

	private int createBucket(boolean isCheck) {
		logger.info("check create bucket : {}", config.getBucket());
		try {
			client.createBucket(config.getBucket());
			if (isCheck) {
				client.deleteBucket(config.getBucket());
			}
			return NO_ERROR;
		} catch (AmazonServiceException ase) {
			if (ase.getErrorCode().compareToIgnoreCase(BUCKET_ALREADY_OWNED_BY_YOU) == NO_ERROR) {
				return NO_ERROR;
			} else if (ase.getErrorCode().compareToIgnoreCase(BUCKET_ALREADY_EXISTS) == NO_ERROR) {
				return NO_ERROR;
			}

			errCode = ase.getErrorCode();
			switch (errCode) {
			case INVALID_ACCESS_KEY_ID:
				if (isCheck) {
					if (isSource) {
						logger.error(LOG_SOURCE_INVALID_ACCESS);
						errMessage = LOG_SOURCE_INVALID_ACCESS;
					} else {
						logger.error(LOG_TARGET_INVALID_ACCESS);
						errMessage = LOG_TARGET_INVALID_ACCESS;
					}
				} else {
					if (isSource) {
						logger.error(LOG_SOURCE_INVALID_ACCESS);
						errMessage = LOG_SOURCE_INVALID_ACCESS;
					} else {
						logger.error(LOG_TARGET_INVALID_ACCESS);
						errMessage = LOG_TARGET_INVALID_ACCESS;
					}
				}
				return INVALID_ACCESS_KEY;
				
			case SIGNATURE_DOES_NOT_MATCH:
				if (isCheck) {
					if (isSource) {
						logger.error(LOG_SOURCE_INVALID_SECRET);
						errMessage = LOG_SOURCE_INVALID_SECRET;
					} else {
						logger.error(LOG_TARGET_INVALID_SECRET);
						errMessage = LOG_TARGET_INVALID_SECRET;
					}
				} else {
					if (isSource) {
						logger.error(LOG_SOURCE_INVALID_SECRET);
						errMessage = LOG_SOURCE_INVALID_SECRET;
					} else {
						logger.error(LOG_TARGET_INVALID_SECRET);
						errMessage = LOG_TARGET_INVALID_SECRET;
					}
				}
				return INVALID_SECRET_KEY;
			}

			logger.error("{}", ase.getMessage());
			errMessage = ase.getMessage();
			return FAILED_CREATE_BUCKET;
        } catch (IllegalArgumentException e) {
			logger.error(e.getMessage());
			errMessage = e.getMessage();
			return FAILED_CREATE_BUCKET;
		}
	}

	private boolean createBucket(String bucket) {
		try {
			client.createBucket(bucket);
			return true;
		} catch (AmazonServiceException ase) {
			if (ase.getErrorCode().compareToIgnoreCase(BUCKET_ALREADY_OWNED_BY_YOU) == NO_ERROR) {
				return true;
			} else if (ase.getErrorCode().compareToIgnoreCase(BUCKET_ALREADY_EXISTS) == NO_ERROR) {
				return true;
			}
			
			logger.error("{} - {}", ase.getErrorCode(), ase.getMessage());
			errMessage = ase.getErrorCode() + " - " + ase.getMessage();
			return false;
        }
	}

	@Override
    public String getErrMessage() {
		return errMessage;
	}

	@Override
	public String getErrCode() {
		return errCode;
	}
}
