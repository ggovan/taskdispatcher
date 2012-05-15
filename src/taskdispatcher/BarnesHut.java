package taskdispatcher;

//Start the applet and define a few necessary variables

import taskdispatcher.threaded.ThreadedDispatcher;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BarnesHut {

	static final double G = 6.673e-11;   // gravitational constant
	static final double solarmass=1.98892e30;
	static final double timestep = 0.001;
	double radius = 1e18;     
	Tree tree;

	public static void main(String[] args){
		try{
			boolean bh = "barnes".equals(args[0]);
			if(!bh&&!"brute".equals(args[0]))
				throw new IllegalArgumentException();
			BarnesHut hut = new BarnesHut();
			List<Body> bodies;
			int numSteps;
			if(args.length==2){
				bodies=hut.readFile(args[1]);
				numSteps=20;
			}
			else{
				int numBodies = Integer.parseInt(args[1]);
				numSteps = Integer.parseInt(args[2]);
				bodies = hut.startthebodies(numBodies);
			}
			//System.out.println(bodies.size()+"\t"+hut.energy(bodies));
			long time = System.currentTimeMillis();
			for(int i = 0 ; i < numSteps; i++){
				if(bh)
					hut.barnesHutStep(bodies, timestep);
				else
					hut.bruteForceStep(bodies, timestep);
			}
			time = System.currentTimeMillis()-time;
			System.out.println(time);
			//System.out.println(bodies.size()+"\t"+hut.energy(bodies)+"\t" + time);
		}
		catch(Exception e){
			System.out.println("Please make your input of the form:");
			System.out.println("[brute|barnes] num_bodies num_steps");
			System.out.println("or : ");
			System.out.println("[brute|barnes] filename");
		}
	}

	public ArrayList<Body> readFile(String filename)throws Exception{
		try{
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String[] line = in.readLine().split(" ");
		int n = Integer.parseInt(line[0]);
		ArrayList<Body> bodies = new ArrayList<>(n);
		for(int i=0;i<n;i++){
			double mass = Double.parseDouble(line[i+1]);
			double x = Double.parseDouble(line[i+1+n]);
			double y = Double.parseDouble(line[i+1+2*n]);
			double z = Double.parseDouble(line[i+1+3*n]);
			double vx = Double.parseDouble(line[i+1+4*n]);
			double vy = Double.parseDouble(line[i+1+5*n]);
			double vz = Double.parseDouble(line[i+1+6*n]);
			Body b = new Body(x,y,z,mass,vx,vy,vz);
			bodies.add(b);
		}
		return bodies;
		}
		catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	public ArrayList<Body> startthebodies(int numBodies){
		ArrayList<Body> bodies = new ArrayList<>(numBodies);
		bodies.add(new Body(0, 0, 1e6 * solarmass, 0, 0));//put a heavy body in the center
		for (int i = 1; i < numBodies; i++) {
			double px = radius * exp(-1.8) * (.5 - Math.random());
			double py = radius * exp(-1.8) * (.5 - Math.random());
			double magv = circlev(px, py);

			double absangle = Math.atan(Math.abs(py / px));
			double thetav = Math.PI / 2 - absangle;
			//double phiv = Math.random() * Math.PI;
			double vx = -1 * Math.signum(py) * Math.cos(thetav) * magv;
			double vy = Math.signum(px) * Math.sin(thetav) * magv;
			//Orient a random 2D circular orbit

			if (Math.random() <= .5) {
				vx = -vx;
				vy = -vy;
			}
			double mass = Math.random() * solarmass * 10 + 1e20;
			//Color a shade of blue based on mass
			bodies.add(new Body(px, py, mass, vx, vy));
		}
		return bodies;
	}

	//A function to return an exponential distribution for position
	public static double exp(double lambda) {
		return -Math.log(1 - Math.random()) / lambda;
	}
	//the bodies are initialized in circular orbits around the central mass.
	//This is just some physics to do that
	public static double circlev(double rx, double ry) {
		double r2 = Math.sqrt(rx * rx + ry * ry);
		double numerator = (6.67e-11) * 1e6 * solarmass;
		return Math.sqrt(numerator / r2);
	}

	void bruteForceStep(Collection<Body> bodies, double timestep){
		for(Body main: bodies){
			for(Body secondary : bodies){
				if(main!=secondary){
					main.addForce(secondary);
				}
			}
		}
		for(Body b : bodies){
			b.update(timestep);
		}
	}

	void barnesHutStep(List<Body> bodies, double timestep){
		//TODO set up tree
		double minx=0,miny=0,minz=0,maxx=0,maxy=0,maxz=0.0;
		for(Body b: bodies){
			minx=maxx=b.x;
			miny=maxy=b.y;
			minz=maxz=b.z;
			break;
		}
		for(Body b: bodies){
			if(b.x<minx)
				minx = b.x;
			else if(b.x>maxx)
				maxx=b.x;
			if(b.y<miny)
				miny = b.y;
			else if(b.y>maxy)
				maxy=b.y;
			if(b.z<minz)
				minz = b.z;
			else if(b.z>maxz)
				maxz=b.z;
		}
		maxx-=minx;
		maxy-=miny;
		maxz-=minz;
		radius = maxx>maxy?maxx>maxz?maxx:maxy>maxz?maxy:maxz:maxy>maxz?maxy:maxz;
		tree = new Tree3d(minx+maxx/2,miny+maxy/2,minz+maxz/2,radius);

		for(Body b : bodies){
			tree.insert(b);
		}
//		ThreadedDispatcher<BarnesHutJob> td = new ThreadedDispatcher<>();
//		td.setUp();
//		for(int i=0;i<1;i++){
//			Collection<Body> b = bodies.subList((i*bodies.size())/8,((i+1)*bodies.size())/8);
//			BarnesHutJob job = new BarnesHutJob();
//			job.setID(""+i);
//			job.bodies=b;
//			td.addJob(job);
//		}
//		td.start();
//		td.end();
		for(Body b : bodies){
			tree.updateForce(b);
		}
		for(Body b : bodies){
			b.update(timestep);
		}
	}

	private double energy(List<Body> bodies) {
		double dx, dy, dz, distance;
		double e = 0.0;

		for(int i = 0; i < bodies.size(); i++){
			Body b= bodies.get(i);
			e += 0.5 * b.mass
					* (b.vx * b.vx + b.vy * b.vy + b.vz * b.vz);
			for (int j = i + 1; j < bodies.size(); ++j) {
				Body b2 = bodies.get(j);
				dx = b.x - b2.x;
				dy = b.y - b2.y;
				dz = b.z - b2.z;

				distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
				e -= (b.mass * b2.mass) / distance;
			}
		}
		return e;
	}

	private class Tree{
		Tree[] quads;
		double x;
		double y;
		double z;
		double size;
		Body body;
		Body com;
		
		public Tree(double x,double y,double size){
			this.x=x;
			this.y=y;
			this.size=size;
			com = new Body(x,y,0.0,0.0,0.0);
			com.x=x;
			com.y=y;
			com.mass=0.0;
		}

		public void update(Body b){
			if(com.mass!=0.0){
				com.x = (com.x*com.mass + b.x*b.mass)/com.mass;
				com.y = (com.y*com.mass + b.y*b.mass)/com.mass;
				com.z = (com.z*com.mass + b.z*b.mass)/com.mass;
			}
			else{
				com.x = b.x;
				com.y = b.y;
				com.z = b.z;
			}

			com.mass = com.mass + b.mass;
		}

		public void insert(Body b){
			update(b);

			if(body==null&&quads==null)
				body=b;
			else if(body != null){
				quads=newQuads();
				put(body);
				put(b);
			}
			else{
				put(b);
			}
		}

		public Tree[] newQuads(){
			Tree[] trees = new Tree[4];
			trees[0]=new Tree(x+size/2, y+size/2, size/2);
			trees[1]=new Tree(x-size/2, y+size/2, size/2);
			trees[2]=new Tree(x-size/2, y-size/2, size/2);
			trees[3]=new Tree(x+size/2, y-size/2, size/2);
			return trees;
		}

		public void put(Body b){
			if(b.x>x&&b.y>y)
				quads[0].insert(b);
			else if(b.x<x&&b.y>y)
				quads[1].insert(b);
			else if(b.x<x&&b.y<y)
				quads[2].insert(b);
			else if(b.x>x&&b.y<y)
				quads[3].insert(b);
		}

		public void updateForce(Body b) {
			if(quads==null){
				if(body!=b&&body!=null)
					b.addForce(body);
			}
			else if(size/body.distanceTo(b)<2){
				b.addForce(com);
			}
			else {
				for(Tree q:quads)
					q.updateForce(b);
			}
		}
	}

	private class Tree3d extends Tree{
		Tree3d(double x, double y, double z, double size){
			super(x,y,size);
			this.z=z;
			com.z=z;
		}
		@Override
		public Tree[] newQuads(){
			Tree[] trees = new Tree[8];
			trees[0]=new Tree3d(x+size/2, y+size/2, z+size/2, size/2);
			trees[1]=new Tree3d(x-size/2, y+size/2, z+size/2, size/2);
			trees[2]=new Tree3d(x-size/2, y-size/2, z+size/2, size/2);
			trees[3]=new Tree3d(x+size/2, y-size/2, z+size/2, size/2);
			trees[4]=new Tree3d(x+size/2, y+size/2, z-size/2, size/2);
			trees[5]=new Tree3d(x-size/2, y+size/2, z-size/2, size/2);
			trees[6]=new Tree3d(x-size/2, y-size/2, z-size/2, size/2);
			trees[7]=new Tree3d(x+size/2, y-size/2, z-size/2, size/2);
			return trees;
		}
		@Override
		public void put(Body b){
			if(b.z>z){
				if(b.x>x&&b.y>y)
					quads[0].insert(b);
				else if(b.x<x&&b.y>y)
					quads[1].insert(b);
				else if(b.x<x&&b.y<y)
					quads[2].insert(b);
				else if(b.x>x&&b.y<y)
					quads[3].insert(b);
			}
			else{
				if(b.x>x&&b.y>y)
					quads[4].insert(b);
				else if(b.x<x&&b.y>y)
					quads[5].insert(b);
				else if(b.x<x&&b.y<y)
					quads[6].insert(b);
				else if(b.x>x&&b.y<y)
					quads[7].insert(b);
			}
		}
	}

	private class Body{
		double x;
		double y;
		double z;
		double mass;
		double vx, vy, vz;
		double fx, fy, fz;

		Body(double x, double y, double mass, double vx, double vy){
			this.x=x;
			this.y=y;
			this.mass=mass;
			this.vx=vx;
			this.vy=vy;
		}
		Body(double x, double y, double z, double mass, double vx, double vy, double vz){
			this.x=x;
			this.y=y;
			this.z=z;
			this.mass=mass;
			this.vx=vx;
			this.vy=vy;
			this.vz=z;
		}

		void update(double dt){
			vx += dt * fx / mass;
			vy += dt * fy / mass;
			vz += dt * fz / mass;
			x += dt * vx;
			y += dt * vy;
			z += dt * vz;
			fx=fy=fz=0.0;
		}

		public void addForce(Body b){
			double EPS = 3E4;      // softening parameter (just to avoid infinities)
			double dx = b.x - x;
			double dy = b.y - y;
			double dz = b.z - z;
			double dist = Math.sqrt(dx*dx + dy*dy + dz*dz + 0.01);
			double force = (G * mass * b.mass) / (dist*dist + EPS*EPS);
			fx += force * dx / dist;
			fy += force * dy / dist;
			fz += force * dz / dist;
		}

		public double distanceTo(Body b){
			double dx = b.x-x;
			double dy = b.y-y;
			double dz = b.z-z;
			return Math.sqrt(dx*dx+dy*dy+dz*dz);
		}

	}

	private class BarnesHutJob implements Job{
		String id;
		Collection<Body> bodies;
		@Override
		public void setID(String ID) {
			id = ID;
		}

		@Override
		public String getID() {
			return id;
		}

		@Override
		public void run() {
			for(Body b : bodies){
				tree.insert(b);
			}
		}
		
	}

}