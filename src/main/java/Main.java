import org.pwman.PasswordManager;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class Main {

    public static final int DEBUG_FLAG = 1;

    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException {
        PasswordManager passwordManager;
        switch (DEBUG_FLAG) {
            case 0:
                passwordManager = new PasswordManager();
                passwordManager.addPassword("test1", "Google");
                passwordManager.addPassword("test2", "Apple");
                passwordManager.addPassword("test3", "Microsoft");
                for (String sealedObject : passwordManager.getSealedPasswords().orElseThrow()) {
                    System.out.println(sealedObject);
                }
                System.out.println();
                System.out.println(passwordManager.getSealedEncryptor());
                break;
            case 1:
                String sealedEncryptor = "W3FBcEZZbFpsVjlFaU9ZRmpiRmdLOGdwVnRFL3AyanNDQVBKeEFlVG9qQlE9LCArZmU1VHUySzR4Z3FPb1lUTis2VHBRPT0sIEFFU10=";
                String[] sealedPasswords = {
                        "HSf6+wHSaaY1Q7/DoWGjQF0UJZhUh+hQ4sdhfQIqa68CLEaXSaL1kb2Qcou3NJWQ3Phpvz3vvT/KWQXQwVTUsPwibjPyCkv392WB/LsGYgyjWskfn3zB7+DcNpCbq0ERbCbLdwP+2d0Dld1ajKANgDnlWvD44N0LLwShX6djuLuKqDoqRMEbNIMP6lCAdqJ2111N+LYDVqo3qxsJdy6OMgZNe2MP0IxyS1cFmjXCMcK08Tekum9EGRpdrrHPzi2OUyOEZXg0ZSFH6/de5crK9d8knYH45sIR56lToGp1wX8E6s5KgR/5q/1wR13gOA2EJ+LlIJpe0WY6vCIHpnvU19jGAjILqkgWg76c+DNgt3i4jSDcMizkBqYe1eNUKB++OK7Zis+s4juvBTk+1QIFpiK8bS/Mrlfhr0GXUQS2bo2EVBDaZiXuD2Cjux6c5jPJQtZKNUC3/G/rt22tf15J2pHrFz9t8MYZr3esl/270g1pyTiZtQAJjJfHOX0nTbZopomnnRd8tETIwsl6DdTxu7R5KSljw+WiOBLhXpfDpE6l1Uoekq7J15uRIgvHoK6k6nKOlrDGhCBO7QLFHNDxN2iKgj9TPeAPCuLKIMjRKkEAXvGwneKXMnLXLD0XtlIJq5anaiR3NTyLa1SS4t+BN0DNGXDwa7rLvt24QbTq9/pfeT5dU/Gepk7diJI7jJ2iKe+HsHH+tGesfph0twHna7afttdG33DogmrRElGSlUOruVsIS5LPKelPzN3LPt0yVG/s1TCwO7WSRSYoUCQTWG/VtcMHXx3GuYBP4loDfVzWjCL2zg2P9cV7oA2QDyS49u6dZGVnWly5r6sXC5LXEKYd2whlMc1/R7jqQ56Thoii+jX/snXlvf5Ufj+z8lnCuYadFZnpZQ+aZeYYVleWCw==",
                        "HSf6+wHSaaY1Q7/DoWGjQF0UJZhUh+hQ4sdhfQIqa68CLEaXSaL1kb2Qcou3NJWQ3Phpvz3vvT/KWQXQwVTUsPwibjPyCkv392WB/LsGYgyjWskfn3zB7+DcNpCbq0ERbCbLdwP+2d0Dld1ajKANgDnlWvD44N0LLwShX6djuLuKqDoqRMEbNIMP6lCAdqJ2111N+LYDVqo3qxsJdy6OMgZNe2MP0IxyS1cFmjXCMcK08Tekum9EGRpdrrHPzi2OUyOEZXg0ZSFH6/de5crK9d8knYH45sIR56lToGp1wX8E6s5KgR/5q/1wR13gOA2EJ+LlIJpe0WY6vCIHpnvU19jGAjILqkgWg76c+DNgt3i4jSDcMizkBqYe1eNUKB++OK7Zis+s4juvBTk+1QIFpiK8bS/Mrlfhr0GXUQS2bo2EVBDaZiXuD2Cjux6c5jPJ64mYSDfacS+qvkUqgb3Tpl8Yv5yfq/Z8ENP+rA18jGGWWIxJeBiknN3QMfIiwbRjKxYfvUbXCU+mwiJtCFWFBImxXLIqaPscTFEQCEvQNf08/bM3G+7WBmeturusId8gzqfqiSUdGTrvI5Wx304wD/Ea51SO4qFEQT/BPWsoIWiNIIShcEPMV4ycRaXzb6y979lys8nAmwaeMEJiZju4Rfx6ZtJSV/x8whegl5gsCo/dsL/gYUewJt5mWrOqXshWbWhZi8JAXU/4D8+BtjfObbEJ/iLrCQA/njBKbVavbUWRKuO/N3W0fUHakGyUmOufsqznU5vd1W1AB16bVjuAFQ7XfV+Mn8CY6bLt1tyi+jCeWOp9NJDM1e/ABE7oGL7XWPsFOstylAqbfNoeTER8X1upEi60nO7yY2wjdkzPQgD2rbJhD4MIAq6n7OluXCbhWSFaKE1uHj10So2DWwYgsg==",
                        "HSf6+wHSaaY1Q7/DoWGjQF0UJZhUh+hQ4sdhfQIqa68CLEaXSaL1kb2Qcou3NJWQ3Phpvz3vvT/KWQXQwVTUsPwibjPyCkv392WB/LsGYgyjWskfn3zB7+DcNpCbq0ERbCbLdwP+2d0Dld1ajKANgDnlWvD44N0LLwShX6djuLuKqDoqRMEbNIMP6lCAdqJ2111N+LYDVqo3qxsJdy6OMgZNe2MP0IxyS1cFmjXCMcK08Tekum9EGRpdrrHPzi2OUyOEZXg0ZSFH6/de5crK9d8knYH45sIR56lToGp1wX8E6s5KgR/5q/1wR13gOA2EJ+LlIJpe0WY6vCIHpnvU19jGAjILqkgWg76c+DNgt3i4jSDcMizkBqYe1eNUKB++OK7Zis+s4juvBTk+1QIFpiK8bS/Mrlfhr0GXUQS2bo2EVBDaZiXuD2Cjux6c5jPJJeRGYgykVFxa8npBuP80xSAiz5PXYNDS2VPDY5t79NVh6KSYQfwzkxZl71WiPCjRdeqHnW+TMskvvc12oY1N0+O203SdQeeXxwNWVR8bLKwtfKKY/8rHeZjHwGRMiebfdpngngD8i+DJ+r3/sYjl/Xd9z9AAXWtndDaTaCx1yi6MWasqhV1buogxSHKGkcO62RUCeDFKstjBL7ZzdGgf/pv2Iw+Zh+/5gg3RMYE4GQYAzvuGp42I4teDNjG35QDrEXLaYFWVb3uFz9SNQ8EzvKR76gaeeJt9WiVATT4Z9BrT7VLV1pKCNKw08Yzh/GVnpI9ZGPn2UAXnG4eQ3xapi3S4aB5heVOpCRbWGss07jzlgjlesAVm/6+/5QA/Q/cJ2TnnyfZdw9MdcPuvIbfDrZHJRKPwB0B9nB/Vaj/xBpkMkArFv731/k1Fkx71Z37q1bzB/i1Mh0V+Y3Su1BYU9w=="
                };
                passwordManager = new PasswordManager(sealedEncryptor, sealedPasswords);
                System.out.println(passwordManager.countPasswords());
                passwordManager.addPassword("test4", "Meta");
                passwordManager.addPassword("test5", "Netflix");
                System.out.println(passwordManager.countPasswords());
                System.out.println();
                for (String platform : passwordManager.getPlatforms()) {
                    System.out.printf("%s: %s%n", platform, passwordManager.getDecryptedPassword(platform).orElse(null));
                }
                break;
        }
    }

}