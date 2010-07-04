package scalatohoku.android.example.tetris

import android.os._
import android.widget._
import android.view._
import android.content._
import android.app._
import service._
import android.view.KeyEvent._
import scalatohoku.android.example.R

class TetrisActivity extends Activity {

  var gamearea:TextView = null
  
  /**
   * エントリメソッド
   */
  override def onCreate(savedInstanceState: Bundle):Unit = {
    super.onCreate(savedInstanceState)
    
    // res/layout/tetris.xml
    setContentView(R.layout.tetris)
    
    
    //Main画面からデータ受取
    val now = getIntent.getExtras.get("NOW").asInstanceOf[java.util.Date]
    findViewById(R.id.infoarea).asInstanceOf[TextView].setText(now.toString)
    
    //ゲーム表示エリア取得
    gamearea = findViewById(R.id.gamearea).asInstanceOf[TextView]
    
    //ボタンイベント
    findViewById(R.id.go_button).asInstanceOf[Button]
    .setOnClickListener( new View.OnClickListener{
    	def onClick(view:View) = goTetris
    })
  }
  
  /**
   * テトリスの開始
   * 
   * このメソッドを書くのに紆余曲折あり。失敗例は末尾に。
   */
  private def goTetris = {
    bindService( new Intent(classOf[ITetrisService].getName), 
    		serviceConnection, Context.BIND_AUTO_CREATE )
  }

  
  /**
   * リモートサービスの接続・切断
   */
  private var tetrisService:ITetrisService = null
  private val serviceConnection = new ServiceConnection {
    def onServiceConnected(className: ComponentName, service: IBinder) {
      tetrisService = ITetrisService.Stub.asInterface(service)
      tetrisService.addListener( listener )
    }
    def onServiceDisconnected(className: ComponentName) {
      tetrisService.removeListener( listener )
      tetrisService = null
    }
  }

  /**
   * リモートサービスからのプッシュ受信（コールバック）
   */
  private val listener = new ITetrisCallbackListener.Stub {
	  def receiveChangedView(view:String):Unit={
	 	  //（失敗例３）これだと同じ問題。子スレッドは親スレッドのViewを触れない！
	 	  //gamearea.setText(view)
	 	  //ので、handlerを仲介する。
	 	  handler.sendMessage(handler.obtainMessage(CALLBACK_MESSAGE, view))
	  }
  }

  /**
   * ハンドラ
   * 
   * 子スレッドからのメッセージ受信を監視して、メインスレッド上で取得する。
   * これが一番大事。
   */
  val CALLBACK_MESSAGE = 1
  private val handler = new Handler {
	  override def dispatchMessage(msg:Message)={
	 	  msg.what match {
	 	 	  case CALLBACK_MESSAGE =>
	 	 	   gamearea.setText(msg.obj.asInstanceOf[String])
	 	 	  case _ =>
	 	 	   super.dispatchMessage(msg)
	 	  }
	  }
  }
  
  /**
   * キー操作をサービスへ伝播
   */
  override def onKeyDown(keyCode:Int, event:KeyEvent):Boolean = {
     if( tetrisService != null ){
	   keyCode match {
		 case KEYCODE_DPAD_RIGHT => tetrisService.setOperation( Tetris.GO_RIGHT )
		 case KEYCODE_DPAD_LEFT => tetrisService.setOperation( Tetris.GO_LEFT )
		 case KEYCODE_DPAD_UP  => tetrisService.setOperation( Tetris.ROTATE )
		 case _ => ()
	   }
       true
     }else 
       super.onKeyDown(keyCode,event)
  }
  
} // end of class


  /* （失敗例１）
   * 子スレッドから親スレッドのViewを触らせるとException
   * EventListener系は同じ問題で引っかかるはず。
   * 
   * Only the original thread that created a view hierarchy can touch its views

  private def goTetris = {
    Tetris.setRenderer( new Renderer {
    	def write(view:String) = gamearea.setText(view)
    })
    Tetris.start
  }
   */
  
  
  /* （失敗例２）
   * 
   * Actorを試したがActorをnewした時点でNullPointerException
   * at scala.actors.Actor$class.searchMailbox(Actor.scala:437)

import scala.actors._
import scala.actors.Actor._

  private def goTetris = {
    val act = new ViewRenderActor
    Tetris.setRenderer( act )
    act.start
    actor{ 
    	Tetris.start
    }
  }
  class ViewRenderActor extends Actor {
    def act {
      loop {
        react {
          case 'GAMEOVER => ()
          case view:String  =>
            gamearea.setText(view)
          case _ =>()
        }
      }
    }
  }
*/

