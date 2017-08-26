import java.util.*;

public class FrameList {
	ArrayList<EthernetFrame> list;

	public FrameList() {
		this.list = new ArrayList<EthernetFrame>();
	}

	public void add(EthernetFrame frame) {
		this.list.add(frame);
	}

	public EthernetFrame nextFrame() {
		EthernetFrame frame = this.list.get(0);
		this.list.remove(0);
		return frame;
	}
}