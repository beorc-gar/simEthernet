public class Device {
	private EthernetFrame pending;
	private int id;
	private static Boolean cable = false;
	private static double cableAvailNext = 0.0;
	private static EthernetFrame cableFrame;

	public Device(int id) {
		this.id = id;
	}

	public Boolean hasPending() {
		return this.pending != null;
	}

	public EthernetFrame getFrame() {
		return this.pending;
	}

	public int getId() {
		return this.id;
	}

	public Boolean addFrame(EthernetFrame frame) {
		if(this.hasPending()) {
			return false;
		}

		this.pending = frame;
		frame.own(this.id);
		return true;
	}

	public Boolean listen() {
		return this.cable;
	}

	public Boolean send(){
		if(this.cable) {
			return false;
		}
		this.cableAvailNext = this.pending.finishTime();
		this.cableFrame = this.pending;
		this.cable = true;
		return true;
	}

	public void cableUse() {
		this.cable = true;
	}

	public double cableAvailTime() {
		return this.cableAvailNext;
	}

	public void sent() {
		this.cable = false;
		this.cableFrame = null;
		this.cableAvailNext = 0.0;
		this.pending = null;
	}

	public EthernetFrame frameUse() {
		return this.cableFrame;
	}
}