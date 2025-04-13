package com.offsec.nhterm;

import com.offsec.nhterm.xorg.NeoXorgViewClient;

/**
 * @author kiva
 */

public class NeoXorgSettings {
  public static void init(NeoXorgViewClient client) {
    Settings.Load(client);
  }
}
