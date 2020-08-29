package com.therdnotes.swagger.eventscounter

import java.io.{File, PrintWriter}

import com.therdnotes.swagger.eventscounter.models._
import io.circe.generic.auto._
import org.slf4j.{Logger, LoggerFactory}
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.docs.openapi._
import sttp.tapir.json.circe._
import sttp.tapir.openapi.{Contact, Info, Server}
import sttp.tapir.openapi.circe.yaml._

object SwaggerGenerator {

  val logger: Logger = LoggerFactory.getLogger("SwaggerGenerator")
  final val TAG_COUNTS: String = "Counts"
  final val TAG_EVENTS: String = "Events"

  def main(args: Array[String]): Unit = {
    logger.info("Starting swagger generation")
    type AuthToken = String
    val unAuthMapping = statusMapping(StatusCode.Unauthorized, jsonBody[ErrorInfo].description("Un-Authorized").example(examples.getError(401, "UNAUTHORIZED")))


    val baseEndpoint = endpoint
      .in(auth.bearer[String])


    // get counts
    val getCounts =
      baseEndpoint
        .get
        .info(EndpointInfo(Some("getCounts"), Some("Gets the counts"), None, List(TAG_COUNTS).toVector, deprecated = false))
        .in("counts")
        .in(queryParams.assetIDParameter)
        .in(queryParams.eventIDParameter)
        .out(jsonBody[GetCounts].example(examples.getCountsResponse))
        .errorOut(
          oneOf(
            unAuthMapping
          )
        )

    // post counts
    val postCounts =
      baseEndpoint
        .post
        .info(EndpointInfo(Some("postCounts"), Some("Submit the counts to add/subtract for different asset/event combination"), None, List(TAG_COUNTS).toVector, deprecated = false))
        .in("counts")
        .in(jsonBody[PostCountsRequest].example(examples.postCountsRequest))
        .out(jsonBody[GetCounts].example(examples.getCountsResponse))
        .errorOut(
          oneOf(
            unAuthMapping
          )
        )


    // ========= generate yaml ============
    val apiInfo = Info(
      title = "Events Counter",
      version = "v1.0.0",
      description = Some(
        """
          |API documentation for Events Counter <br/>
          |<br/>
          |<p>Some terms you should know</p>
          |<p> - Asset: It is something on which you are going to have an event. Eg: a web page, a screen in mobile app, a button. It can be anything. </p>
          |<p> - Event: The event you want to count. Eg: page views, button clicks etc. </p>
          |<p> - Count: The number representing the count for an event. </p>
          |<p> - Step: It is the number by which to increment or decrement the count. Tip: To decrement by one pass <code>-1</code> </p>
          |""".stripMargin),
      contact = Some(Contact(
        name = Some("Events Counter"),
        email = None,
        url = Some("https://www.therdnotes.com/projects/events-counter")
      ))
    )
    val openAPI = List(
      getCounts,
      postCounts
    )
      .toOpenAPI(apiInfo)
      .servers(List(
        Server("http://localhost:8080/").description("Local Dev")
      ))

    val file = new File("docs/events-counter.yaml")
    println(file.getAbsolutePath)
    val printWriter = new PrintWriter(file)
    printWriter.write(openAPI.toYaml)
    printWriter.close()
    logger.info("Ending swagger generation")

  }

}

object queryParams {
  val assetIDParameter: EndpointInput.Query[Option[String]] = query[Option[String]]("asset_id").description("Unique ID of the Asset")
  val eventIDParameter: EndpointInput.Query[Option[String]] = query[Option[String]]("event_id").description("Unique ID of the Event")
  val limitParameter: EndpointInput.Query[Option[String]] = query[Option[String]]("limit").description("Limit the number of records to be fetched")
}

object examples {
  private val contextId = "848b30d5-0d6e-42e6-845e-92e3eb87dff3"
  private val assetIdHomePage = "home-page"
  private val assetIdTerminalSetup = "blog/terminal-setup"
  private val eventIdPageViews = "page-views"
  private val eventIdUpVote = "up-vote"
  private val eventIdClaps = "claps"

  private val homePageViewsCount = Count(assetIdHomePage, eventIdPageViews, 80482)
  private val articleUpVoteCount = Count(assetIdTerminalSetup, eventIdUpVote, 240)
  private val articleClapsCount = Count(assetIdTerminalSetup, eventIdClaps, 4028)

  private val homePageViewsStep = Step(assetIdHomePage, eventIdPageViews, 1)
  private val articleUpVoteStep = Step(assetIdTerminalSetup, eventIdUpVote, -1)
  private val articleClapsStep = Step(assetIdTerminalSetup, eventIdClaps, 40)

  val getCountsResponse: GetCounts = GetCounts(counts = List[Count](homePageViewsCount, articleUpVoteCount, articleClapsCount))
  val postCountsRequest: PostCountsRequest = PostCountsRequest(List[Step](homePageViewsStep, articleUpVoteStep, articleClapsStep))

  def getError(code: Int, msg: String): ErrorInfo = ErrorInfo(contextId = contextId, code = code, message = msg)
}

object models {

  case class Count(assetId: String, eventId: String, count: Int)

  case class Step(assetId: String, eventId: String, stepBy: Int)

  case class GetCounts(counts: List[Count])

  case class PostCountsRequest(counts: List[Step])

  case class ErrorInfo(contextId: String, code: Int, message: String)

}
