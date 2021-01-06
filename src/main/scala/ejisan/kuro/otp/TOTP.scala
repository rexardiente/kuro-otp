package ejisan.kuro.otp

import java.net.URI
import scala.collection.JavaConverters._
import scala.compat.java8.OptionConverters._

class TOTP(
    val algorithm: OTPAlgorithm,
    val digits: Int,
    val period: Int,
    val initialTimestamp: Long,
    val otpkey: OTPKey) extends TOTPSupport {

  def currentTime(): Long =
    System.currentTimeMillis() / 1000

  def generate(): String = generate(currentTime())
  def generate(instantTimestamp: Long): String = {
    intToDigits(generateForTime(
        algorithm,
        digits,
        period,
        initialTimestamp,
        otpkey,
        instantTimestamp),
      digits)
  }

  def generate(instantTimestamp: Long, window: Int): Map[Long, String] = {
    generateForTime(
        algorithm,
        digits,
        period,
        initialTimestamp,
        otpkey,
        instantTimestamp,
        window)
      .mapValues(intToDigits(_, digits)).toMap
  }

  def generateAsJava(instantTimestamp: Long, window: Int): java.util.Map[java.lang.Long, String] = {
    generate(instantTimestamp, window)
      .map({ case (k, v) => (Long.box(k), v) })
      .asJava
  }

  def validate(code: String): Boolean = validate(currentTime(), code)


  def validate(instantTimestamp: Long, code: String): Boolean = {
    validateWithTime(
      algorithm,
      digits,
      period,
      initialTimestamp,
      otpkey,
      instantTimestamp,
      digitsToInt(code))
  }


  def validate(window: Int, code: String): Option[Long] = validate(currentTime(), window, code)

  def validate(instantTimestamp: Long, window: Int, code: String): Option[Long] = {
    validateWithTime(
      algorithm,
      digits,
      period,
      initialTimestamp,
      otpkey,
      instantTimestamp,
      window,
      digitsToInt(code))
  }

  def validateAsJava(window: Int, code: String): java.util.OptionalLong =
    validateAsJava(currentTime(), window, code)

  def validateAsJava(instantTimestamp: Long, window: Int, code: String): java.util.OptionalLong =
    validate(instantTimestamp, window, code).asPrimitive

  def toURI(
      account: String,
      issuer: Option[String] = None,
      params: Map[String, String] = Map()): URI = {
    val p = Map(
      "digits" -> digits.toString,
      "period" -> period.toString,
      "algorithm" -> algorithm.name)
    OTPAuthURICodec.encode(
      protocol,
      account,
      otpkey,
      issuer,
      params ++ p)
  }
  def toURI(
      account: String,
      issuer: java.util.Optional[String],
      params: java.util.Map[String, String]): URI =
    toURI(account, issuer.asScala, params.asScala.toMap)

  override def toString: String =
    s"TOTP(${otpkey.toBase32}, ${algorithm.name}, $digits, $period, $initialTimestamp)"

  override def hashCode() = {
    41 * (
      41 * (
        41 * (
          41 * otpkey.hashCode + algorithm.hashCode) +
        digits.hashCode) +
      period.hashCode) +
    initialTimestamp.hashCode
  }

  override def equals(obj: Any): Boolean = obj match {
    case o: TOTP =>
      o.otpkey == otpkey &&
      o.algorithm == algorithm &&
      o.digits == digits &&
      o.period == period &&
      o.initialTimestamp == initialTimestamp
    case _ => false
  }
}

object TOTP {
  def apply(
      algorithm: OTPAlgorithm,
      digits: Int,
      period: Int,
      initialTimestamp: Long,
      otpkey: OTPKey): TOTP = {
    new TOTP(algorithm, digits, period, initialTimestamp, otpkey)
  }


  def apply(
      algorithm: OTPAlgorithm,
      digits: Int,
      period: Int,
      otpkey: OTPKey): TOTP =
    apply(algorithm, digits, period, 0l, otpkey)

  def getInstance(
      algorithm: OTPAlgorithm,
      digits: Int,
      period: Int,
      initialTimestamp: Long,
      otpkey: OTPKey): TOTP =
    apply(algorithm, digits, period, initialTimestamp, otpkey)


  def getInstance(
      algorithm: OTPAlgorithm,
      digits: Int,
      period: Int,
      otpkey: OTPKey): TOTP =
    apply(algorithm, digits, period, 0l, otpkey)

  def fromURI(uri: URI): TOTP = {
    import scala.util.control.Exception.allCatch
    OTPAuthURICodec.decode(uri) match {
      case Some(decoded) =>
        apply(
          decoded.params.get("algorithm").flatMap(OTPAlgorithm.find).getOrElse(OTPAlgorithm.SHA1),
          decoded.params.get("digits").flatMap(d => allCatch.opt(d.toInt)).getOrElse(6),
          decoded.params.get("period").flatMap(d => allCatch.opt(d.toInt)).getOrElse(6),
          decoded.otpkey)
      case None => throw new IllegalArgumentException("Illegal URI given.")
    }
  }
}
