package finch.archerhud.hud;

import finch.archerhud.eOutAngle;
import finch.archerhud.eOutType;

public abstract class HUDAdapter implements HUDInterface {
    @Override
    public final void setCurrentTime(int nH, int nM) {
        setTime(nH, nM, false, false, true, false);
    }

    @Override
    public final void setDirection(eOutAngle nDir) {
        setDirection(nDir, eOutType.Lane, eOutAngle.AsDirection);
    }

}
