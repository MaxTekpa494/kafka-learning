package generator;

import com.github.javafaker.Faker;
import model.TransactionRecord;

import java.util.Random;

public class TransactionRecordGenerator {
    private static final int MARGEPRIX = 50;
    private static final int MINPRIX = 10;
    private static final int NBSTORES = 6;

    private final Faker faker = new Faker();
    private final Random random = new Random();

    public TransactionRecord generate() {
        TransactionRecord record = new TransactionRecord();
        record.setNom(faker.name().lastName());
        record.setPrenom(faker.name().firstName());
        record.setIdProduit(5 + random.nextInt(10));

        float marge = random.nextInt(2 * MARGEPRIX) - MARGEPRIX;
        float prix = Math.round(MINPRIX * (1 + marge / 100f) * 100) / 100f;
        record.setPrix(prix);

        record.setStoreId(1 + random.nextInt(NBSTORES));
        return record;
    }
}
