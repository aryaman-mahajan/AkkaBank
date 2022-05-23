import Bank.{AccountClosed, CloseAccount, CreateBankAccount, Deposit, GetDetails, Withdraw}
import Main.system
import SenderActor.StartMessages
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

object Main extends App {
  val system = ActorSystem("BankSystem")
  protected def createBank(): ActorRef = system.actorOf(Bank.props, name="bank")
  private val bank = createBank()
  protected def createSenderActor(): ActorRef = system.actorOf(SenderActor.props(bank))
  private val senderActor = createSenderActor()

  senderActor ! SenderActor.StartMessages

  system.terminate()
}

object SenderActor{
  def props(bank: ActorRef): Props = Props(new SenderActor(bank: ActorRef))
  case object StartMessages
}

class SenderActor(bank: ActorRef) extends Actor with ActorLogging{
  import SenderActor._

  override def receive: Receive = {
    case StartMessages =>{
      bank ! CreateBankAccount("Aryaman","10/07/1999","Chandigarh","9914646655")
      bank ! CreateBankAccount("Shubham", "01/08/2000","Panchkula", "9876543210")
      bank ! GetDetails(10000)
      bank ! GetDetails(10001)
      bank ! Deposit(1000, 10000)
      bank ! Deposit(1000, 10001)
      bank ! GetDetails(10000)
      bank ! GetDetails(10001)
      bank ! Withdraw(100, 10000)
      bank ! Deposit(1000, 10001)
      bank ! GetDetails(10000)
      bank ! GetDetails(10001)
      bank ! Withdraw(1000, 10000)
      bank ! Deposit(10000, 10001)
      bank ! GetDetails(10000)
      bank ! GetDetails(10001)
      bank ! Withdraw(1000, 10005)
      bank ! CloseAccount(10000)
      bank ! Withdraw(100,10000)
    }
    case Bank.BankAccountCreated(accountID) => log.debug(s"Your Bank Account is created with ID - $accountID")
    case Bank.AccountBalance(amount) => log.debug(s"$amount is the Account Balance")
    case Bank.AccountClosed(accountID) => log.debug(s"Your Bank Account is closed with ID - $accountID")
    case Bank.DepositSuccessful(amount, accountID) => log.debug(s"Deposited $amount in your Account with ID - $accountID ")
    case Bank.DepositFailed(failureReason) => log.debug(failureReason)
    case Bank.InvalidAccountID => log.debug("Account ID dosen't exist")
  }
}