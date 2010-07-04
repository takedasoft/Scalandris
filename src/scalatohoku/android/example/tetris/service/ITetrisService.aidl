package scalatohoku.android.example.tetris.service;

import scalatohoku.android.example.tetris.service.ITetrisCallbackListener;

interface ITetrisService {
  void setOperation(int op);
  
  void addListener(ITetrisCallbackListener listener);
  void removeListener(ITetrisCallbackListener listener);

}
