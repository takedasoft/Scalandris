package scalatohoku.android.example.tetris

import scala.collection.mutable.ArrayBuffer

trait ArrayBufferUtil {
	
  /**
   * construct ArrayBuffer[String](size)
   */
  implicit def supliment( ab:ArrayBuffer[Int] ) = new {
	  def updateBy(seq:IndexedSeq[Int]):ArrayBuffer[Int] = {
	 	  (0 until ab.size) foreach( i=>  ab(i) = seq(i))
	 	  ab
	  }
  }
  
  
  def $S(size:Int)={
    val b = new ArrayBuffer[String] //initialSizeが効かない
    b.padTo(size, "") //のでpadToで初期化
  }
  def $I(size:Int)={
    val b = new ArrayBuffer[Int]
    b.padTo(size, 0)
  }
  
  //have same type after erasure: (a: Seq)
  def $$(a:ArrayBuffer[Int]*) = new ArrayBuffer[ArrayBuffer[Int]]() ++= a
  def $(a:Int*) = new ArrayBuffer[Int]() ++= a

  def debug(str:String){
	  println(str)
  }
  def debug(seq:Seq[_]){
	  println( seq.mkString(",") )
  }
}