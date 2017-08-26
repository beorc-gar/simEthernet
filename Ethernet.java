import java.util.*;
import java.text.*;

public class Ethernet {
    private static ArrayList<Device> available(ArrayList<Device> all) {
        ArrayList<Device> available = new ArrayList<Device>();
        for(Device d : all) {
            if(!d.hasPending()) {
                available.add(d);
            }
        }
        return available;
    }
	
    public static void main(String[] args) {
		long seed           = 0;
		int numberOfDevices = 0;
        int numberOfFrames  = 0;
        int droppedFrames   = 0;
        double arrivalRate  = 0.0;
        double arrivalTime  = 0.0;
        double bytesSent    = 0.0;
        double throughput   = 0.0;
        double prevTime     = 0.0;
        double size         = 0.0;
        DecimalFormat df    = new DecimalFormat("#.#");
        EventList events    = new EventList();
        FrameList frames    = new FrameList();
        Random rng;
        ArrayList<Device> devices = new ArrayList<Device>();
		
		try {
            seed            = Integer.parseInt(args[0]);
            numberOfDevices = Integer.parseInt(args[1]);
            numberOfFrames  = Integer.parseInt(args[2]);
            arrivalRate     = Double.parseDouble(args[3]);
            arrivalTime     = 2560/arrivalRate;
        } catch(NumberFormatException e) {
            System.out.println("Arguments should be positive real numbers.");
            System.exit(-1);
        } catch(ArrayIndexOutOfBoundsException e) {
            System.out.println("Please provide 4 arguments.");
            System.exit(-1);
        } catch(ArithmeticException e) {
        	System.out.println("arrival_rate should not be 0.");
            System.exit(-1);
        }

        System.out.println("Input:\tSeed from user = "+seed);

        if(seed == 0) {
            seed = System.currentTimeMillis();
        }
        rng = new Random(seed);

        System.out.println("\tSeed to use = "+seed);
        System.out.println("\t# devices = "+numberOfDevices);
        System.out.println("\t# frames to simulate = "+numberOfFrames);
        System.out.println("\tMean # arrival frames/100 slot times = "+df.format(arrivalRate));
        System.out.println("\tMean arrival time = "+df.format(arrivalTime));

        for(int i = 0; i < numberOfDevices; i++) {
        	devices.add(new Device(i));
        }

        double arrive = 0.0;
        for(int i = 0; i < numberOfFrames; i++) {
        	arrive += rng.nextDouble()*2*arrivalRate;
        	size    = ((Math.log(1-rng.nextDouble())/(-0.5))*1454)+72;
        	EthernetFrame frame = new EthernetFrame(arrive, size, i+1);
        	frames.add(frame);
        	events.add(new Event(arrive, frame, EventType.ARRIVE));
        }

        while(!events.complete()) {
       		Event next = events.next();
       		Event todo;
            ArrayList<Device> availDevices = available(devices);
            Device dev = devices.get(next.getFrame().owner());
        	
        	if(next.typeOfEvent() == EventType.ARRIVE) {
                int index = rng.nextInt(availDevices.size());
                dev = availDevices.get(index);

                if(availDevices.size() == 0) {
                    System.out.println("Devices already have pending frames. Can't assign this frame to a device.");
                    System.exit(-1);
                }
        		do {
        			dev = devices.get(rng.nextInt(numberOfDevices));
        		} while(dev.hasPending());
        	} else if(next.typeOfEvent() == EventType.SENT) {
        		bytesSent+=next.getFrame().size();
        	}
        	
        	dev.addFrame(next.getFrame());
        	todo = next.execute(dev);
        	
        	if(todo.typeOfEvent() == EventType.COLLISION) {
        		events.collision(dev.frameUse());
        		System.out.println("Frame "+dev.frameUse().getId()+" sender detects "+dev.frameUse().getCollisions()+"th collision at "+df.format(next.time())+" mus");
        		if(dev.frameUse().getCollisions() <= 10) {
        			double backoff = rng.nextDouble()*51.2*Math.pow(2.0, dev.frameUse().getCollisions()*1.0);
        			events.add(new Event(next.time()+backoff, dev.frameUse(), EventType.LISTEN));
        			System.out.println("\t\tbacks off for "+df.format(backoff)+" mus");
        		} else {
        			droppedFrames++;
                    System.out.println("Frame "+dev.frameUse().getId()+" dropped at "+df.format(next.time())+" mus");
        		}
        		events.collision(next.getFrame());
        		System.out.println("Frame "+next.getFrame().getId()+" sender detects "+next.getFrame().getCollisions()+"th collision at "+df.format(next.time())+" mus");
        		if(next.getFrame().getCollisions() <= 10) {
        			double backoff = rng.nextDouble()*51.2*Math.pow(2.0, next.getFrame().getCollisions()*1.0);
        			events.add(new Event(next.time()+backoff, next.getFrame(), EventType.LISTEN));
        			System.out.println("\t\tbacks off for "+df.format(backoff)+" mus");
        		} else {
        			droppedFrames++;
                    System.out.println("Frame "+next.getFrame().getId()+" dropped at "+df.format(next.time())+" mus");
        		}
        	} else if(todo.typeOfEvent() == EventType.FIRST72) {
        		if(next.time() > prevTime) {
        			dev.cableUse();
        		}
        	}
        	if(todo.typeOfEvent() != EventType.NONE && todo.typeOfEvent() != EventType.COLLISION) {
        		events.add(todo);
        	} else {
                dev.sent();
            }
        	prevTime = next.time();
        }

        throughput = bytesSent/(prevTime*1000000);

        System.out.println("Performance Report:");
        System.out.println("\t# simulated frames = "+numberOfFrames+" frames");
        System.out.println("\t# bytes successfully sent = "+df.format(bytesSent)+" bytes");
        System.out.println("\t# dropped frames = "+droppedFrames+" frames");
        System.out.println("\t# Ethernet throughput = "+throughput+" bps = "+df.format(throughput*1000000)+" Mbps");
	}
}