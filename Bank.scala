import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.collection.mutable.ListBuffer

object Bank{
  def props : Props = Props(new Bank)
  case class CreateBankAccount(name:String, dob:String, sex:String, mobile:String)
  case class BankAccountCreated(accountID: Int)
  case class Deposit(amount:Int, accountID: Int)
  case class DepositSuccessful(amount:Int, accountID: Int)
  case class DepositFailed(string: String)
  case class Withdraw(amount:Int, accountID: Int)
  case class GetDetails(accountID: Int)
  case class AccountBalance(amount:Int)
  case class CloseAccount(accountID: Int)
  case class AccountClosed(accountID: Int)
  case object InvalidAccountID
}

class Bank extends Actor with ActorLogging{
  import Bank._
  log.debug("Bank is Open")

  private var accountNo: Int = 10000
  private var accountIDs: Map[Int, ActorRef] = Map.empty
  private var accountBook: Map[Int, Int] = Map.empty
  private var accountDetails: Map[Int, ListBuffer[String]] = Map.empty
  private val accountLimit: Int =
    context.system.settings.config.getInt("bank.account.limit")
  protected def createBankAccount(): ActorRef = {
    context.actorOf(BankAccount.props)
  }

  override def receive: Receive = {
      case CreateBankAccount(name:String, dob:String, address:String, mobile:String) => {
        val bankAccount = createBankAccount()
        val accountID = accountNo
        accountDetails += accountID -> ListBuffer(name, dob, address, mobile)
        accountNo += 1
        accountIDs += accountID -> bankAccount
        accountBook += accountID -> 0
        log.info(s"Account ID - $accountID created")
        sender() ! BankAccountCreated(accountID)
      }
      case Deposit(amount, accountID) if !accountBook.contains(accountID) =>{
        log.info(s"Account ID - $accountID dosen't exist")
        sender() ! InvalidAccountID
      }
      case Deposit(amount, accountID) if accountBook(accountID) + amount <= accountLimit => {
        accountBook += accountID -> (accountBook(accountID) + amount)
        accountIDs(accountID) ! AccountBalance(accountBook(accountID))
        log.info(s"$amount deposited in Account ID - $accountID")
        sender() ! DepositSuccessful(amount, accountID)
      }
      case Deposit(amount, accountID) => {
        log.info(s"Sorry Account ID - $accountID but you have reached your account limit")
        sender() ! DepositFailed(s"Sorry Account ID - $accountID but you have reached your account limit")
      }
      case Withdraw(amount, accountID) if !accountBook.contains(accountID) =>{
        log.info(s"Account ID - $accountID dosen't exist")
        sender() ! InvalidAccountID
      }
      case Withdraw(amount, accountID) if accountBook(accountID) - amount >= 0 => {
        accountBook += accountID -> (accountBook(accountID) - amount)
        accountIDs(accountID) ! AccountBalance(accountBook(accountID))
        log.info(s"$amount withdrawn from Account ID - $accountID")
      }
      case Withdraw(amount, accountID) => {
        log.info(s"Sorry Account ID - $accountID but you have insufficient funds")
      }
      case GetDetails(accountID) if !accountBook.contains(accountID) =>{
        log.info(s"Account ID - $accountID dosen't exist")
        sender() ! InvalidAccountID
      }
      case GetDetails(accountID) => {
        log.info(s"Name - ${accountDetails(accountID).head}, DOB - ${accountDetails(accountID)(1)}, Address - ${accountDetails(accountID)(2)}, Mobile Number - ${accountDetails(accountID)(3)}, Account ID - $accountID  your bank balance is ${accountBook(accountID)}")
        sender() ! AccountBalance(accountBook(accountID))   //Send balance to sender
      }
      case CloseAccount(accountID) if !accountBook.contains(accountID) =>{
        log.info(s"Account ID - $accountID dosen't exist")
        sender() ! InvalidAccountID
      }
      case CloseAccount(accountID) => {
        log.info(s"Thanks for being our customer Account ID - $accountID")
        context.stop(accountIDs(accountID))
        accountBook -= accountID
        accountIDs -= accountID
        accountDetails -= accountID
        sender() ! AccountClosed(accountID)
      }




      case BankAccount.BalanceUpdated => log.debug("Amount Updates in BankAccount Actor")
  }
}