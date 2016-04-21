package com.autoscout24.utils.s3

import java.util.concurrent.TimeUnit
import com.typesafe.config.ConfigFactory
import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ListVersionsRequest

import scala.concurrent.duration.Duration

object Main {

  val startTime = System.nanoTime()

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()

    if (!config.hasPath("region") || !config.hasPath("bucketName")) {
      println("Example usage: sbt run -Dregion=eu-west-1 -DbucketName=my-bucket-name")
    } else {
      val region = config.getString("region")
      val bucketName = config.getString("bucketName")

      deleteBucket(region, bucketName)
    }
  }

  def deleteBucket(region: String, bucketName: String): Unit = {
    val s3client = new AmazonS3Client(new EnvironmentVariableCredentialsProvider())
    s3client.setRegion(Region.getRegion(Regions.fromName(region)))

    try {
      println("Deleting S3 bucket: " + bucketName)
      var objectListing = s3client.listObjects(bucketName)
      var objectCounter = 0

      while (objectListing != null) {
        val iterator = objectListing.getObjectSummaries.iterator()

        while (iterator.hasNext) {
          val objectSummary = iterator.next()
          println("Deleting object: " + objectSummary.getKey + " O: " + objectCounter + " " + timeElapsed)
          objectCounter += 1
          s3client.deleteObject(bucketName, objectSummary.getKey)
        }

        if (objectListing.isTruncated) {
          objectListing = s3client.listNextBatchOfObjects(objectListing)
        } else {
          objectListing = null
        }
      }

      var list = s3client.listVersions(new ListVersionsRequest().withBucketName(bucketName))
      var versionsIterator = list.getVersionSummaries.iterator()
      var versionsCounter = 0

      while (list != null) {
        while (versionsIterator.hasNext) {
          val s = versionsIterator.next()
          println("Deleting version: " + s.getVersionId + " " + versionsCounter + " " + timeElapsed)
          versionsCounter += 1
          s3client.deleteVersion(bucketName, s.getKey, s.getVersionId)
        }

        if (list.isTruncated) {
          list = s3client.listNextBatchOfVersions(list)
          versionsIterator = list.getVersionSummaries.iterator()
        } else {
          list = null
        }
      }

      println("Deleting the bucket")
      s3client.deleteBucket(bucketName);
    } catch {
      case ase: AmazonServiceException =>
        println("Caught an AmazonServiceException, which " +
          "means your request made it " +
          "to Amazon S3, but was rejected with an error response" +
          " for some reason.")
        println("Error Message:    " + ase.getMessage())
        println("HTTP Status Code: " + ase.getStatusCode())
        println("AWS Error Code:   " + ase.getErrorCode())
        println("Error Type:       " + ase.getErrorType())
        println("Request ID:       " + ase.getRequestId());
      case ace: AmazonClientException =>
        println("Caught an AmazonClientException, which " +
          "means the client encountered " +
          "an internal error while trying to " +
          "communicate with S3, " +
          "such as not being able to access the network.")
        println("Error Message: " + ace.getMessage());
    }
  }

  def timeElapsed: String = {
    Duration(System.nanoTime() - startTime, TimeUnit.NANOSECONDS).toString()
  }
}
