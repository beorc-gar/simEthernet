import java.text.*;

public class Event {
	private double timeOccur;
	private EthernetFrame frame;
	private EventType type;

	public Event(double time, EthernetFrame frame, EventType type) {
		this.timeOccur = time;
		this.frame     = frame;
		this.type      = type;
	}

	public EthernetFrame getFrame() {
		return this.frame;
	}

	public double time() {
		return this.timeOccur;
	}

	public EventType typeOfEvent() {
		return this.type;
	}

	public Event execute(Device device) {
		DecimalFormat df = new DecimalFormat("#.#");
		switch(this.type) {
			case ARRIVE:
				device.addFrame(this.frame);
				System.out.println("Frame "+this.frame.getId()+" arrives @ "+df.format(this.timeOccur)+" mus");
				System.out.println("\tF"+this.frame.getId()+": len="+df.format(this.frame.size())+" dlvtm="+df.format(this.frame.time())+" sdr="+this.frame.owner()+" listen arr@"+df.format(this.timeOccur)+" col="+this.frame.getCollisions());

				return new Event(this.timeOccur+1.0, this.frame, EventType.LISTEN);
			case LISTEN:
				String state = "quiet: sends";
				Event ret;
				
				if(device.listen() && this.timeOccur < device.cableAvailTime()) {
					state = "busy";
					ret = new Event(device.cableAvailTime(), this.frame, EventType.LISTEN);
				} else {
					if(!device.send()) {
						this.frame.detectCollision();
						ret = new Event(this.timeOccur+3686.4, this.frame, EventType.COLLISION);
					} else {
						ret = new Event(this.timeOccur+3686.4, this.frame, EventType.FIRST72);
					}
				}
				System.out.println("Frame "+this.frame.getId()+" sender listens @ "+df.format(this.timeOccur)+" mus ... "+state);
				return ret;

			case COLLISION:
				break;
			case FIRST72:
				System.out.println("Frame "+this.frame.getId()+" 1st 72 bytes delivered @ "+df.format(this.timeOccur)+" mus");
				return new Event(this.timeOccur+(51.2*this.frame.size()/72), this.frame, EventType.SENT);
			case SENT:
				device.sent();
				System.out.println("Frame "+this.frame.getId()+" sending is complete @ "+df.format(this.timeOccur)+" mus");
				this.type = EventType.NONE;
		}
		return this;
	}
}