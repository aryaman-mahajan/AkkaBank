import Bank.{AccountBalance, CloseAccount, CreateBankAccount, Deposit, GetDetails, Withdraw}
import BankAccount.BalanceUpdated
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActors, TestKit, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class BankAccountSpec()
  extends TestKit(ActorSystem("BankSpec"))
    with ImplicitSender
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A BankAccount actor" must {
    "update when money deposited/withdrawn" in {
      val sender = TestProbe()
      val bankAccount = system.actorOf(Props[BankAccount])
      sender.send(bankAccount, AccountBalance(1000))

      sender.expectMsg(BalanceUpdated)
    }

  }
}
