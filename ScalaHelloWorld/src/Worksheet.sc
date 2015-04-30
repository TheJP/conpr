trait Func[I,O] {
	def exec(in: I): O
}

trait Action[I] extends Func[I,Unit] {
}

object Worksheet {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  41 + 1                                          //> res0: Int(42) = 42
  
}