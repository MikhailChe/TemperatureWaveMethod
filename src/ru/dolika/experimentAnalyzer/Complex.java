package ru.dolika.experimentAnalyzer;

public class Complex {
	public double im, re;

	public Complex(double re) {
		this(re, 0);
	}

	public Complex(double re, double im) {
		this.im = im;
		this.re = re;
	}

	public Complex() {
		this(0, 0);
	}

	// (a+bi)+(c+di)=(a+c)+(b+d)i.
	public Complex add(Complex c) {
		Complex out = new Complex();
		out.re = this.re + c.re;
		out.im = this.im + c.im;
		return out;
	}

	// (a+bi)-(c+di)=(a-c)+(b-d)i.
	public Complex subt(Complex c) {
		Complex out = new Complex();
		out.re = this.re - c.re;
		out.im = this.im - c.im;
		return out;
	}

	// (a+bi)*(c+di)=ac+bci+adi+bdi^2=(ac-bd)+(bc+ad)i.
	public Complex mult(Complex c) {
		Complex out = new Complex();
		out.re = (this.re * c.re - this.im * this.im);
		out.im = (this.re * c.im + this.im * c.re);
		return out;
	}

	public Complex div(Complex c) {
		Complex out = new Complex();
		out.re = (this.re * c.re + this.im * c.im)
				/ (c.re * c.re + c.im * c.im);
		out.im = (this.im * c.re - this.re * c.im)
				/ (c.re * c.re + c.im * c.im);
		return out;
	}

	public double absSQ() {
		return this.re * this.re + this.im * this.im;
	}

	public double abs() {
		return Math.sqrt(absSQ());
	}

	public double arg() {
		return Math.atan2(this.im, this.re);

	}
}
