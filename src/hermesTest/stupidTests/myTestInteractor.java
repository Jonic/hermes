package src.hermesTest.stupidTests;

import src.hermes.*;

public class myTestInteractor implements Interactor<myTestBeing, myTestBeing> {

	@Override
	public boolean detect(myTestBeing being1, myTestBeing being2) {
		System.out.println(being1 + " " + being2);
		return false;
	}

	@Override
	public void handle(myTestBeing being1, myTestBeing being2) {
		System.out.println(being1 + " " + being2);
	}

}
