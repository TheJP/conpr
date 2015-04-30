import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class NiceAtomicInt(init: Int) {
  val ref = new AtomicInteger(init)

  def modify(f: Int => Int): Unit = {
    val i = ref.get
    ref.compareAndSet(i, f(i))
  }
}

class NiceAtomic[A](init: A) {
  val ref = new AtomicReference[A](init)

  def modify(f: A => A): Unit = {
    val old = ref.get
    ref.compareAndSet(old, f(old))
  }
}

object HelloWorld {

  def clock(p: Int): Unit = {
    println("Tick(" + p + ")")
  }

  def everySecond(action: Int => Unit): Unit = {
    val t = new Thread(){
      override def run(): Unit = {
        var i = 0
        while(true){
          action(i)
          i += 1
          Thread.sleep(1000)
        }
      }
    }.start()
  }

  def time(block: => Unit): Unit = {
    val start = System.currentTimeMillis()
    block
    println("[" + (System.currentTimeMillis() - start) + "]")
  } 

  def main(args: Array[String]): Unit = {
    println("dlrow olleh".reverse)
    clock(3)
    //everySecond(clock)
    time ({
      Thread.sleep(100)
    })
    time {
      Thread.sleep(100)
    }
    val b = new NiceAtomicInt(0)
    b.modify { b => b+10 }
    println(b.ref.get)
    val c = new NiceAtomic[List[String]](List("a", "b"))
    c.modify { l => l.reverse }
    println(c.ref.get)
  }
}