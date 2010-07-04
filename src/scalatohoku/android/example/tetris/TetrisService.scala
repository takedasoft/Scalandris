package scalatohoku.android.example.tetris

import android.os._
import android.app.Service
import android.content.Intent
import android.os.IBinder
import scala.concurrent.ops._
import service._

/**
 * リモートサービスの作り方：
 * 
 * 1. ITetrisService.aidl を作成。
 * 2. ITetrisService.java が自動生成
 * 3. ITetrisService.Stub をサービス側に実装
 * 4. AndroidManifest.xml に登録
 * 5. ServiceConnection をクライアント側に実装して、ITetrisService.Stubを仲介してサービスコール
 */
class TetrisService extends Service {
	
	private val listeners = new RemoteCallbackList[ITetrisCallbackListener]
	
	private val binder:ITetrisService.Stub = new ITetrisService.Stub() {
		// remote methods
		def setOperation( op: Int ):Unit = {
			Tetris.op( op )
		}
		def addListener(l:ITetrisCallbackListener):Unit={
			listeners.register(l)
		}
		def removeListener(l:ITetrisCallbackListener):Unit={
			listeners.unregister(l)
		}
	}
	def onBind(itent:Intent):IBinder = {
		start
		binder
	}
	
	/**
	 * テトリスの画面をクライアントにプッシュ
	 */
	private def pushView(view:String){
		val clientSize = listeners.beginBroadcast
		(0 until clientSize).foreach( i => {
			listeners.getBroadcastItem(i).receiveChangedView(view)
		})
		listeners.finishBroadcast
	}
	
	/**
	 * テトリスの開始
	 */
	def start() = {
		Tetris.setRenderer( new Renderer {
			def write(view:String) = pushView(view)
		})
		Tetris.start
	}
	
	override def onDestroy() = {
	}
}