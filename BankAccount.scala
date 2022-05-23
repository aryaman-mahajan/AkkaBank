import akka.actor.{Actor, ActorLogging, Props}

object BankAccount{
  def props : Props = Props(new BankAccount)
  case object BalanceUpdated
}

class BankAccount extends Actor with ActorLogging{
  import BankAccount._
  private var accountBalance = 0

  override def receive: Receive = {
    case Bank.AccountBalance(amount) => {
      accountBalance = amount
      sender() ! BalanceUpdated
    }

  }
}
