package scalatohoku.android.example.tetris

import scala.actors._
import scala.actors.Actor._
import java.util.Timer
import java.util.TimerTask
import scala.collection.mutable.ArrayBuffer

/**
 * テトリスの実装。
 * 
 * "わずか565バイトテトリスのプログラミング解説"
 * 
 * から移植してみました。実装不完全です。
 * 
 * @see http://zapanet.info/blog/item/1125
 *
 */
object Tetris extends ArrayBufferUtil {
  
  /*　表示先の設定 */
  var _renderer:Renderer = null
  def setRenderer(renderer:Renderer){
    _renderer = renderer
  }
  
  def renderViewBuffer()={
    val s = _viewBuffer.mkString //grouped(12)/sliding(12)が効かない..
    val rendered = (0 until 240 by 12).foldLeft(""){ (str,i) => str + s.substring(i,i+12)+"\n" }
    _renderer.write(rendered)
  }
  def copy( from:IndexedSeq[Int], to:ArrayBuffer[Int] ) = {
	 (0 until to.size) foreach( i=>  to(i) = from(i))
  }

  /* ユーザー操作 */
  val GO_RIGHT = 1
  val GO_LEFT = 2
  val ROTATE = 3
  val NONE = 0
  var _operation = NONE
  def op(op:Int) = { _operation = op }
  
  /*　メインループ */
  def start() = loop
  def loop(){
    _buffer(11) = _point.toString // 得点を表示バッファに書き込み 
    var block = _blocks( _current ) // 現在落下中のブロック 

    _operation match {
      case GO_RIGHT => // 右移動判定 
      	if( (true /: PIXELS){ (b,i) => b && ( _buffer( _center + block(i) + 1 ) != WALL ) } ){
   		  _center += 1
      	}
      case GO_LEFT =>  // 左移動判定
      	if( (true /: PIXELS){ (b,i) => b && ( _buffer( _center + block(i) - 1 ) != WALL ) } ){
   		  _center -= 1
      	}
      case ROTATE => //回転判定
        //回転先の座標を計算
        val afterRotate:IndexedSeq[Int] = _current match {
        	case 0 => block //四角ブロックは計算いらない
        	case _ => (0 until 4) map( i => {
        				val p = block(i)
        				val tx = java.lang.Math.round( p/12 )
        				val ty = p - tx*12
        				ty*12-tx })
        }
		if( (true /: PIXELS){ (b,i) => b && ( _buffer( _center + afterRotate(i) ) != WALL ) } ){
			//block updateBy afterRotate //Android上でimplicit defが効かない
			//_blocks(_current) updateBy afterRotate
			copy( afterRotate, block )
			copy( afterRotate, _blocks(_current) )
		}
      case _ => ()
    }
    _operation = NONE // キー入力キャンセル 

    //落下判定
    (true /: PIXELS){ (b,i)=> {
    	val p = _center + block(i)
    	_buffer(240 + p) = WALL
    	b && _buffer(12+p) != WALL
    } } match {
    	case false => 
    		// ブロック停止　// 次のブロック決定（現在順送り） 
    		PIXELS foreach( i => _buffer( _center + block(i) ) = WALL ) 
    		_current = (_current + 1) % 7 
    		_center = 17
    	case _ => 
    		_center += 12
    }
    
    /* 移植できず・・
　　  for(k=1,i=19;i--;){ // ラインがそろったか判定 
　　　　for(j=11;--j&&Z[i*12+j]==S;); // そろったラインを検索
　　　　if(!j){ // そろった 
　　　　　　Point += k++; // 得点 1ライン 1点, ..., テトリス 10点 になる 
　　　　　　for(j=++i*12;j>2*12;){
		　　Z[j]=Z[j---12] // 全体を一段下げる
　　　　　　}
　　	　}
	}
    */
    
    //バッファを表示 
    (0 until 240).reverse.foreach { i => 
      _viewBuffer(i) = _buffer(240+i)
      _buffer(240+i) = _buffer(i)
    }
    renderViewBuffer
    
    //再帰
    if( _buffer(5) != WALL ){
    	/* （失敗例）
    	 * Thread.sleepを使うと、Android上ではまともに動かない。
    	 *
    	Thread.sleep(100)
    	loop
    	 */
    	(new Timer).schedule( new TimerTask {
    		def run() = { loop }
    	}, 100)
    }
  }
  
  /*　インスタンス変数 */
  
  // ブロックの作成 
  // 中心からの差分で配置する。棒を除くすべてのブロックはＬ字にブロックがあり、 
  val _blocks = $$( $(-11), $(-24), $(2), $(13), $(-13), $(-1), $(2,-1) )
  (0 until 7) foreach { i => _blocks(i) += (0,1,-12) } // ブロック共通部分追加 
  val PIXELS = (0 until 4)
  
  var _current = 0 //現在落下中のブロック（のインデックス）
  var _center = 17 // ブロックの中心位置:縦方向１マスは 12 座標(x,y) なら h=x+y*12 
  val _buffer = $S(480) //[0-239]:計算用  [240-479]:表示用バッファ(12 x 20)
  val _viewBuffer = $S(240)
  var _point = 0   //点数
  val WALL = "X"  //壁のキャラクタ
  val FREE = " " //空白のキャラクタ

  /* ゲームステージを作成 */
  (0 until 240) foreach { i =>
    val next = i+1
    val p = if( next % 12 < 2 || next > 228 ) WALL else FREE
    _buffer(240+i) = p
    _buffer(i) = p
  }
}
