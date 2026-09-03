package fr.kvnbbg.tdaah.domain;

/**
 * Refus explicite de diviser par une mesure nulle.
 *
 * <p>C'est le geste qui donne son nom au projet : on ne renvoie ni
 * {@code Infinity}, ni {@code NaN}, ni zéro « par commodité ». Une division par
 * une grandeur nulle n'a pas de résultat, et le dire est plus utile que de
 * propager une valeur que l'appelant croira exploitable.
 *
 * <p>Miroir de {@code ZeroDivisionMeasurementError} dans web/js/convertisseur.js :
 * les deux implémentations doivent refuser dans les mêmes cas.
 */
public class ZeroDivisionMeasurementException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ZeroDivisionMeasurementException() {
        super("division par zéro (dénominateur de mesure nul).");
    }

    public ZeroDivisionMeasurementException(String message) {
        super(message);
    }
}
