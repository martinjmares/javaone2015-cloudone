package we.love.pluto.visualizer;

import java.util.Random;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

/**
 * @author Michal Gajdos
 */
final class Test {

    public static void main(String[] args) throws Exception {
        final String[] words = generateRandomWords(20);
        final WebTarget target = ClientBuilder.newClient().target("http://localhost:5001/space-object/of-the-moment");

        while (true) {
            target.request().post(Entity.text(words[new Random().nextInt(words.length)]));
            Thread.sleep(1000);
        }
    }

    public static String[] generateRandomWords(int numberOfWords) {
        String[] randomStrings = new String[numberOfWords];
        Random random = new Random();
        for (int i = 0; i < numberOfWords; i++) {
            char[] word = new char[random.nextInt(8) + 3]; // words of length 3 through 10. (1 and 2 letter words are boring.)
            for (int j = 0; j < word.length; j++) {
                word[j] = (char) ('a' + random.nextInt(26));
            }
            randomStrings[i] = new String(word);
        }
        return randomStrings;
    }
}
