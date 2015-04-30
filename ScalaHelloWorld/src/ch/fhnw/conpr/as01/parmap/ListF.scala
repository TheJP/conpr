import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.Callable

object ListF {

  val THREADS = 50

  def parMap1[A,B](l: List[A], f: A => B): List[B] = {
    val executor = Executors.newFixedThreadPool(THREADS)
    def exec(el: List[A]): List[Future[B]] = {
      el match {
        case Nil => Nil
        case x :: xs => executor.submit(
          new Callable[B](){
            override def call(): B = {
              f(x)
            }
        }) ::  exec(xs)  
      }
    }
    def await(el: List[Future[B]]): List[B] = {
      el match {
        case Nil => Nil
        case x :: xs => x.get :: await(xs)
      }
    }
    await(exec(l))
  }

  //Tests
  def main(args: Array[String]): Unit = {
    println(parMap1(
      List("a", "b", "c"),
      (s:String) => s + "..OHO!"
    ))
    
  }
}