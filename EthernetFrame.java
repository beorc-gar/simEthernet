public class EthernetFrame {
	private double arrivalTime;
	private int sender;
	private double size;
	private int id;
	private int collisions;

	public EthernetFrame(double arrivalTime, double size, int id) {
		this.arrivalTime = arrivalTime;
		this.size = size;
		this.id = id;
		this.collisions = 0;
	}

	public double time() {
		return 51.6f*this.size/72;
	}

	public void detectCollision() {
		this.collisions++;
	}

	public int getCollisions() {
		return this.collisions;
	}

	public double finishTime() {
		return this.time() + this.arrivalTime;
	}

	public void own(int sender) {
		this.sender = sender;
	}

	public int owner() {
		return this.sender;
	}

	public int getId() {
		return this.id;
	}

	public double size() {
		return this.size;
	}
}