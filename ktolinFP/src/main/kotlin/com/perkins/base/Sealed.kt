sealed class Expr
data class Const(val number: Double) : Expr()
data class Sum(val el: Expr, val e2: Expr) : Expr()
object NotANumber : Expr()