import java.io.IOException;
import java.net.Socket;

public class Client {

    private static ActorOuterClass.Actor actor;

    public static void main(String[] args) throws IOException {
        Socket s = new Socket("localhost", 9999);

        actor = ActorOuterClass.Actor.newBuilder()
                .setName("Christian")
                .setSurname("Bale")
                .setSex(ActorOuterClass.Actor.Sex.MALE)
                .addMovie(ActorOuterClass.Actor.Movie.newBuilder()
                        .setTitle("The Prestige")
                        .setYear(2006))
                .addMovie(ActorOuterClass.Actor.Movie.newBuilder()
                        .setTitle("The Dark Knight")
                        .setYear(2008))
                .build();

        actor.writeTo(s.getOutputStream());

        s.close();


    }
}

