import Bank.{AccountBalance, AccountClosed, BankAccountCreated, CloseAccount, CreateBankAccount, Deposit, DepositFailed, DepositSuccessful, GetDetails, InvalidAccountID, Withdraw}
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActors, TestKit, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class BankSpec()
  extends TestKit(ActorSystem("BankSpec"))
    with ImplicitSender
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
  "A Bank actor" must {
    "initialise with 0" in {
      val sender = TestProbe()
      val bank = system.actorOf(Props[Bank])
      sender.send(bank, CreateBankAccount("Aryaman","10/07/1999","Chandigarh","9914646655"))
      sender.send(bank, GetDetails(10000))

      sender.expectMsg(BankAccountCreated(10000))
      sender.expectMsg(AccountBalance(0))
    }
    "deposit amount should be correct" in {
      val sender = TestProbe()
      val bank = system.actorOf(Props[Bank])
      sender.send(bank, CreateBankAccount("Aryaman","10/07/1999","Chandigarh","9914646655"))
      sender.send(bank, Deposit(1000,10000))
      sender.send(bank, GetDetails(10000))

      sender.expectMsg(BankAccountCreated(10000))
      sender.expectMsg(DepositSuccessful(1000,10000))
      sender.expectMsg(AccountBalance(1000))
    }
    "withdrawal amount should be correct" in {
      val sender = TestProbe()
      val bank = system.actorOf(Props[Bank])
      sender.send(bank, CreateBankAccount("Aryaman","10/07/1999","Chandigarh","9914646655"))
      sender.send(bank, Deposit(1000,10000))
      sender.send(bank,Withdraw(900,10000))
      sender.send(bank, GetDetails(10000))

      sender.expectMsg(AccountBalance(100))
    }
    "Exceed amount limit" in {
      val sender = TestProbe()
      val bank = system.actorOf(Props[Bank])
      sender.send(bank, CreateBankAccount("Aryaman","10/07/1999","Chandigarh","9914646655"))
      sender.send(bank, Deposit(100000,10000))
      sender.send(bank, GetDetails(10000))

      sender.expectMsg(BankAccountCreated(10000))
      sender.expectMsg(DepositFailed("Sorry Account ID - 10000 but you have reached your account limit"))
      sender.expectMsg(AccountBalance(0))
    }
    "insufficient funds" in {
      val sender = TestProbe()
      val bank = system.actorOf(Props[Bank])
      sender.send(bank, CreateBankAccount("Aryaman","10/07/1999","Chandigarh","9914646655"))
      sender.send(bank, Deposit(1000,10000))
      sender.send(bank,Withdraw(1100,10000))
      sender.send(bank, GetDetails(10000))

      sender.expectMsg(AccountBalance(1000))
    }
    "no account after closing" in {
      val sender = TestProbe()
      val bank = system.actorOf(Props[Bank])
      sender.send(bank, CreateBankAccount("Aryaman","10/07/1999","Chandigarh","9914646655"))
      sender.send(bank, CloseAccount(10000))
      sender.send(bank, GetDetails(10000))

      sender.expectMsg(BankAccountCreated(10000))
      sender.expectMsg(AccountClosed(10000))
      sender.expectMsg(InvalidAccountID)
    }
    "invalid account id" in {
      val sender = TestProbe()
      val bank = system.actorOf(Props[Bank])
      sender.send(bank, CreateBankAccount("Aryaman","10/07/1999","Chandigarh","9914646655"))
      sender.send(bank, GetDetails(10005))

      sender.expectMsg(BankAccountCreated(10000))
      sender.expectMsg(InvalidAccountID)
    }
  }
}